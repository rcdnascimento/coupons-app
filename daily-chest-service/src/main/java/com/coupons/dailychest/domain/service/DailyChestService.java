package com.coupons.dailychest.domain.service;

import com.coupons.dailychest.domain.entity.DailyChestOpening;
import com.coupons.dailychest.infra.gateway.profile.ProfileGateway;
import com.coupons.dailychest.infra.messaging.DailyChestBonusKafkaPublisher;
import com.coupons.dailychest.infra.messaging.dto.DailyChestBonusGrantedMessage;
import com.coupons.dailychest.infra.persistence.DailyChestOpeningRepository;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DailyChestService {

    private final DailyChestOpeningRepository dailyChestOpeningRepository;
    private final ProfileGateway profileGateway;
    private final DailyChestBonusKafkaPublisher dailyChestBonusKafkaPublisher;

    public DailyChestService(
            DailyChestOpeningRepository dailyChestOpeningRepository,
            ProfileGateway profileGateway,
            DailyChestBonusKafkaPublisher dailyChestBonusKafkaPublisher) {
        this.dailyChestOpeningRepository = dailyChestOpeningRepository;
        this.profileGateway = profileGateway;
        this.dailyChestBonusKafkaPublisher = dailyChestBonusKafkaPublisher;
    }

    @Transactional(readOnly = true)
    public Optional<DailyChestOpening> today(UUID userId) {
        String timezone = profileGateway.resolveTimezoneByUserId(userId);
        LocalDate localDate = LocalDate.now(toZoneId(timezone));
        return dailyChestOpeningRepository.findByUserIdAndLocalDate(userId, localDate);
    }

    @Transactional(readOnly = true)
    public LocalDate resolveTodayLocalDate(UUID userId) {
        String timezone = profileGateway.resolveTimezoneByUserId(userId);
        return LocalDate.now(toZoneId(timezone));
    }

    @Transactional
    public OpenResult open(UUID userId) {
        String timezone = profileGateway.resolveTimezoneByUserId(userId);
        LocalDate localDate = LocalDate.now(toZoneId(timezone));

        Optional<DailyChestOpening> existing =
                dailyChestOpeningRepository.findByUserIdAndLocalDate(userId, localDate);
        if (existing.isPresent()) {
            return new OpenResult(existing.get(), true);
        }

        int rollValue = ThreadLocalRandom.current().nextInt(1, 101);
        int rewardCoins = toRewardCoins(rollValue);
        String idempotencyKey = "daily-chest:user:" + userId + ":date:" + localDate;

        DailyChestOpening opening = new DailyChestOpening();
        opening.setUserId(userId);
        opening.setLocalDate(localDate);
        opening.setTimezone(timezone);
        opening.setRewardCoins(rewardCoins);
        opening.setRollValue(rollValue);
        opening.setIdempotencyKey(idempotencyKey);

        DailyChestOpening saved;
        try {
            saved = dailyChestOpeningRepository.save(opening);
        } catch (DataIntegrityViolationException ex) {
            DailyChestOpening concurrent =
                    dailyChestOpeningRepository
                            .findByUserIdAndLocalDate(userId, localDate)
                            .orElseThrow(() -> ex);
            return new OpenResult(concurrent, true);
        }

        DailyChestBonusGrantedMessage evt = new DailyChestBonusGrantedMessage();
        evt.setUserId(saved.getUserId());
        evt.setRewardCoins(saved.getRewardCoins());
        evt.setLocalDate(saved.getLocalDate());
        evt.setIdempotencyKey(saved.getIdempotencyKey());
        evt.setSchemaVersion(1);
        dailyChestBonusKafkaPublisher.publish(evt);

        return new OpenResult(saved, false);
    }

    public static int toRewardCoins(int rollValue) {
        if (rollValue <= 80) {
            return 10;
        }
        if (rollValue <= 95) {
            return 50;
        }
        return 100;
    }

    private static ZoneId toZoneId(String timezone) {
        try {
            return ZoneId.of(timezone);
        } catch (Exception ignored) {
            return ZoneId.of("America/Sao_Paulo");
        }
    }

    public static class OpenResult {

        private final DailyChestOpening opening;
        private final boolean alreadyOpened;

        public OpenResult(DailyChestOpening opening, boolean alreadyOpened) {
            this.opening = opening;
            this.alreadyOpened = alreadyOpened;
        }

        public DailyChestOpening getOpening() {
            return opening;
        }

        public boolean isAlreadyOpened() {
            return alreadyOpened;
        }
    }
}
