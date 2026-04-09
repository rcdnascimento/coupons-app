package com.coupons.profile.domain.service;

import com.coupons.profile.domain.entity.Profile;
import com.coupons.profile.domain.entity.ReferralRedemption;
import com.coupons.profile.domain.event.ReferralBonusGrantedEvent;
import com.coupons.profile.domain.exception.InvalidReferralCodeException;
import com.coupons.profile.domain.exception.ProfileAlreadyExistsException;
import com.coupons.profile.domain.exception.ReferralCodeAlreadyUsedException;
import com.coupons.profile.infra.persistence.ProfileRepository;
import com.coupons.profile.infra.persistence.ReferralRedemptionRepository;
import java.security.SecureRandom;
import java.util.UUID;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProfileCreationService {

    private static final String REFERRAL_ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int REFERRAL_CODE_LENGTH = 8;
    private static final int MAX_REFERRAL_GENERATION_ATTEMPTS = 20;

    private final ProfileRepository profileRepository;
    private final ReferralRedemptionRepository referralRedemptionRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final SecureRandom secureRandom = new SecureRandom();

    public ProfileCreationService(
            ProfileRepository profileRepository,
            ReferralRedemptionRepository referralRedemptionRepository,
            ApplicationEventPublisher eventPublisher) {
        this.profileRepository = profileRepository;
        this.referralRedemptionRepository = referralRedemptionRepository;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Cria perfil. Se {@code referralCodeInput} for preenchido, valida de forma síncrona que o código
     * existe, não foi usado noutro registo e não é o próprio utilizador; regista a redenção e publica
     * evento para crédito assíncrono no ledger (após commit).
     */
    @Transactional
    public Profile create(Profile draft, String referralCodeInput) {
        UUID userId = draft.getUserId();
        if (profileRepository.existsById(userId)) {
            throw new ProfileAlreadyExistsException(userId);
        }

        String normalizedReferral = normalizeReferralCode(referralCodeInput);
        Profile referrerProfile = null;
        if (normalizedReferral != null) {
            referrerProfile =
                    profileRepository
                            .findByReferralCode(normalizedReferral)
                            .orElseThrow(
                                    () ->
                                            new InvalidReferralCodeException(
                                                    "Código de indicação inválido."));
            if (referrerProfile.getUserId().equals(userId)) {
                throw new InvalidReferralCodeException("Código de indicação inválido.");
            }
            if (referralRedemptionRepository.existsByReferralCode(normalizedReferral)) {
                throw new ReferralCodeAlreadyUsedException(
                        "Código de indicação já foi utilizado noutro registo.");
            }
        }

        draft.setReferralCode(generateUniqueReferralCode());
        profileRepository.save(draft);
        eventPublisher.publishEvent(new ReferralBonusGrantedEvent(null, userId, null));

        if (normalizedReferral != null && referrerProfile != null) {
            ReferralRedemption redemption = new ReferralRedemption();
            redemption.setReferralCode(normalizedReferral);
            redemption.setReferrerUserId(referrerProfile.getUserId());
            redemption.setReferredUserId(userId);
            referralRedemptionRepository.save(redemption);
            eventPublisher.publishEvent(
                    new ReferralBonusGrantedEvent(
                            referrerProfile.getUserId(), userId, normalizedReferral));
        }

        return draft;
    }

    private String generateUniqueReferralCode() {
        for (int i = 0; i < MAX_REFERRAL_GENERATION_ATTEMPTS; i++) {
            String code = randomReferralCode();
            if (!profileRepository.existsByReferralCode(code)) {
                return code;
            }
        }
        throw new IllegalStateException("Não foi possível gerar código de indicação único");
    }

    private String randomReferralCode() {
        StringBuilder sb = new StringBuilder(REFERRAL_CODE_LENGTH);
        for (int i = 0; i < REFERRAL_CODE_LENGTH; i++) {
            sb.append(REFERRAL_ALPHABET.charAt(secureRandom.nextInt(REFERRAL_ALPHABET.length())));
        }
        return sb.toString();
    }

    /** Normaliza para o formato armazenado (maiúsculas, sem espaços). Vazio → null. */
    private static String normalizeReferralCode(String raw) {
        if (raw == null) {
            return null;
        }
        String t = raw.trim();
        if (t.isEmpty()) {
            return null;
        }
        return t.toUpperCase();
    }
}
