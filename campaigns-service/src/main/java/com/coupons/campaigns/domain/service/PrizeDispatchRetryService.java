package com.coupons.campaigns.domain.service;

import com.coupons.campaigns.domain.PrizeDispatchStatus;
import com.coupons.campaigns.domain.entity.CampaignAllocation;
import com.coupons.campaigns.infra.gateway.prizes.PrizesGateway;
import com.coupons.campaigns.infra.messaging.CampaignEventMarshaller;
import com.coupons.campaigns.infra.messaging.dto.PrizeDistributionRequestEvent;
import com.coupons.campaigns.infra.persistence.CampaignAllocationRepository;
import java.time.Instant;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PrizeDispatchRetryService {

    private final CampaignAllocationRepository campaignAllocationRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final CampaignEventMarshaller eventMarshaller;
    private final PrizesGateway prizesGateway;
    private final String prizeDistributionTopic;
    private final long deliveryConfirmationWaitMs;
    private final int maxDispatchAttempts;
    private final int retryBatchSize;

    public PrizeDispatchRetryService(
            CampaignAllocationRepository campaignAllocationRepository,
            KafkaTemplate<String, String> kafkaTemplate,
            CampaignEventMarshaller eventMarshaller,
            PrizesGateway prizesGateway,
            @Value("${coupons.kafka.topic-prize-distribution-request:prize.distribution.request}")
                    String prizeDistributionTopic,
            @Value("${coupons.distribution.delivery-confirmation-wait-ms:15000}")
                    long deliveryConfirmationWaitMs,
            @Value("${coupons.distribution.max-dispatch-attempts:10}")
                    int maxDispatchAttempts,
            @Value("${coupons.distribution.retry-batch-size:200}")
                    int retryBatchSize) {
        this.campaignAllocationRepository = campaignAllocationRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.eventMarshaller = eventMarshaller;
        this.prizesGateway = prizesGateway;
        this.prizeDistributionTopic = prizeDistributionTopic;
        this.deliveryConfirmationWaitMs = deliveryConfirmationWaitMs;
        this.maxDispatchAttempts = maxDispatchAttempts;
        this.retryBatchSize = retryBatchSize;
    }

    @Scheduled(fixedDelayString = "${coupons.distribution.retry-poll-ms:5000}")
    @Transactional
    public void runPrizeDeliveryRetry() {
        Instant now = Instant.now();
        Instant eligibleBefore = now.minusMillis(deliveryConfirmationWaitMs);

        List<CampaignAllocation> candidates =
                campaignAllocationRepository.findTop200ByDispatchAttemptCountLessThanEqualAndDispatchLastAttemptAtLessThanEqualOrderByDispatchLastAttemptAtAsc(
                        maxDispatchAttempts, eligibleBefore);

        int processed = 0;
        for (CampaignAllocation allocation : candidates) {
            if (processed >= retryBatchSize) {
                break;
            }
            processed++;

            if (allocation.getDispatchStatus() == PrizeDispatchStatus.DELIVERED) {
                continue;
            }

            boolean delivered;
            try {
                delivered =
                        prizesGateway.isPrizeDelivered(
                                allocation.getUserId(),
                                allocation.getCampaignId(),
                                allocation.getCouponId());
            } catch (Exception ex) {
                allocation.setDispatchLastError(ex.getMessage());
                allocation.setDispatchLastAttemptAt(now);
                campaignAllocationRepository.save(allocation);
                continue;
            }

            if (delivered) {
                allocation.setDispatchStatus(PrizeDispatchStatus.DELIVERED);
                allocation.setDispatchLastError(null);
                allocation.setDispatchLastAttemptAt(now);
                campaignAllocationRepository.save(allocation);
                continue;
            }

            try {
                publishPrizeDistribution(allocation);
                allocation.setDispatchStatus(PrizeDispatchStatus.PENDING);
                int attempts =
                        allocation.getDispatchAttemptCount() == null
                                ? 0
                                : allocation.getDispatchAttemptCount();
                allocation.setDispatchAttemptCount(attempts + 1);
                allocation.setDispatchLastAttemptAt(now);
                allocation.setDispatchLastError(null);
                campaignAllocationRepository.save(allocation);
            } catch (Exception ex) {
                allocation.setDispatchStatus(PrizeDispatchStatus.PUBLISH_FAILED);
                int attempts =
                        allocation.getDispatchAttemptCount() == null
                                ? 0
                                : allocation.getDispatchAttemptCount();
                allocation.setDispatchAttemptCount(attempts + 1);
                allocation.setDispatchLastAttemptAt(now);
                allocation.setDispatchLastError(ex.getMessage());
                campaignAllocationRepository.save(allocation);
            }
        }
    }

    private void publishPrizeDistribution(CampaignAllocation allocation) {
        PrizeDistributionRequestEvent event = new PrizeDistributionRequestEvent();
        event.setCampaignId(allocation.getCampaignId());
        event.setUserId(allocation.getUserId());
        event.setCouponId(allocation.getCouponId());
        event.setCouponCode(allocation.getCodeSnapshot());
        event.setOccurredAt(allocation.getAllocatedAt());
        event.setSchemaVersion(1);

        String payload = eventMarshaller.toJson(event);
        String eventKey = eventKey(allocation);

        try {
            kafkaTemplate.send(prizeDistributionTopic, eventKey, payload).get();
        } catch (Exception ex) {
            throw new IllegalStateException(
                    "Falha ao publicar evento prize.distribution.request no Kafka", ex);
        }
    }

    private String eventKey(CampaignAllocation allocation) {
        return allocation.getCampaignId() + ":" + allocation.getUserId() + ":" + allocation.getCouponId();
    }
}
