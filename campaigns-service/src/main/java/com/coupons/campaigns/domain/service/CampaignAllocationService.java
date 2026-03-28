package com.coupons.campaigns.domain.service;

import com.coupons.campaigns.domain.CampaignSubscriptionStatus;
import com.coupons.campaigns.domain.CouponStatus;
import com.coupons.campaigns.domain.PrizeDispatchStatus;
import com.coupons.campaigns.domain.entity.CampaignAllocation;
import com.coupons.campaigns.domain.entity.Coupon;
import com.coupons.campaigns.domain.exception.BadRequestException;
import com.coupons.campaigns.domain.exception.CampaignNotFoundException;
import com.coupons.campaigns.domain.exception.ConflictException;
import com.coupons.campaigns.domain.exception.CouponNotFoundException;
import com.coupons.campaigns.infra.messaging.CampaignEventMarshaller;
import com.coupons.campaigns.infra.messaging.dto.PrizeDistributionRequestEvent;
import com.coupons.campaigns.infra.persistence.CampaignAllocationRepository;
import com.coupons.campaigns.infra.persistence.CampaignCouponRepository;
import com.coupons.campaigns.infra.persistence.CampaignRepository;
import com.coupons.campaigns.infra.persistence.CampaignSubscriptionRepository;
import com.coupons.campaigns.infra.persistence.CouponRepository;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

@Service
public class CampaignAllocationService {

    private final CampaignRepository campaignRepository;
    private final CampaignSubscriptionRepository campaignSubscriptionRepository;
    private final CampaignAllocationRepository campaignAllocationRepository;
    private final CampaignCouponRepository campaignCouponRepository;
    private final CouponRepository couponRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final CampaignEventMarshaller eventMarshaller;
    private final String prizeDistributionTopic;
    private final TransactionTemplate transactionTemplate;

    public CampaignAllocationService(
            CampaignRepository campaignRepository,
            CampaignSubscriptionRepository campaignSubscriptionRepository,
            CampaignAllocationRepository campaignAllocationRepository,
            CampaignCouponRepository campaignCouponRepository,
            CouponRepository couponRepository,
            KafkaTemplate<String, String> kafkaTemplate,
            CampaignEventMarshaller eventMarshaller,
            PlatformTransactionManager transactionManager,
            @Value("${coupons.kafka.topic-prize-distribution-request:prize.distribution.request}")
                    String prizeDistributionTopic) {
        this.campaignRepository = campaignRepository;
        this.campaignSubscriptionRepository = campaignSubscriptionRepository;
        this.campaignAllocationRepository = campaignAllocationRepository;
        this.campaignCouponRepository = campaignCouponRepository;
        this.couponRepository = couponRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.eventMarshaller = eventMarshaller;
        this.prizeDistributionTopic = prizeDistributionTopic;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
        this.transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    public CampaignAllocation allocate(UUID campaignId, UUID userId) {
        return transactionTemplate.execute(
                status -> {
                    campaignRepository
                            .findById(campaignId)
                            .orElseThrow(() -> new CampaignNotFoundException(campaignId));

                    if (!campaignSubscriptionRepository.existsByCampaignIdAndUserIdAndStatus(
                            campaignId, userId, CampaignSubscriptionStatus.ACTIVE)) {
                        throw new BadRequestException("Utilizador não inscrito nesta campanha (inscrição ativa)");
                    }
                    if (campaignAllocationRepository.existsByCampaignIdAndUserId(campaignId, userId)) {
                        throw new ConflictException("Utilizador já tem cupom alocado nesta campanha");
                    }

                    List<UUID> availableCouponIds =
                            campaignCouponRepository.findLinkedCouponIdsNotYetAllocated(campaignId);
                    if (availableCouponIds.isEmpty()) {
                        throw new ConflictException("Não há cupons disponíveis para alocação");
                    }

                    UUID couponId =
                            availableCouponIds.get(
                                    ThreadLocalRandom.current().nextInt(availableCouponIds.size()));
                    Coupon coupon =
                            couponRepository
                                    .findById(couponId)
                                    .orElseThrow(() -> new CouponNotFoundException(couponId));

                    CampaignAllocation allocation = new CampaignAllocation();
                    allocation.setCampaignId(campaignId);
                    allocation.setUserId(userId);
                    allocation.setCouponId(couponId);
                    allocation.setCodeSnapshot(coupon.getCode());
                    allocation.setDispatchStatus(PrizeDispatchStatus.PENDING);
                    allocation.setDispatchAttemptCount(0);
                    allocation.setDispatchLastAttemptAt(null);
                    allocation.setDispatchLastError(null);

                    CampaignAllocation saved = campaignAllocationRepository.save(allocation);

                    coupon.setStatus(CouponStatus.ALLOCATED);
                    couponRepository.save(coupon);

                    PrizeDistributionRequestEvent event = new PrizeDistributionRequestEvent();
                    event.setCampaignId(saved.getCampaignId());
                    event.setUserId(saved.getUserId());
                    event.setCouponId(saved.getCouponId());
                    event.setCouponCode(coupon.getCode());
                    event.setOccurredAt(saved.getAllocatedAt());
                    event.setSchemaVersion(1);

                    String eventKey =
                            saved.getCampaignId()
                                    + ":"
                                    + saved.getUserId()
                                    + ":"
                                    + saved.getCouponId();
                    String payload = eventMarshaller.toJson(event);

                    try {
                        kafkaTemplate.send(prizeDistributionTopic, eventKey, payload).get();
                    } catch (Exception ex) {
                        throw new IllegalStateException(
                                "Falha ao publicar evento prize.distribution.request no Kafka", ex);
                    }

                    Instant now = Instant.now();
                    saved.setDispatchAttemptCount(1);
                    saved.setDispatchLastAttemptAt(now);
                    saved.setDispatchLastError(null);
                    saved.setDispatchStatus(PrizeDispatchStatus.PENDING);
                    return campaignAllocationRepository.save(saved);
                });
    }
}
