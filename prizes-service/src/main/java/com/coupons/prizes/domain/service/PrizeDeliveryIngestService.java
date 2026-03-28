package com.coupons.prizes.domain.service;

import com.coupons.prizes.domain.entity.PrizeDelivery;
import com.coupons.prizes.infra.messaging.PrizeEventMarshaller;
import com.coupons.prizes.infra.messaging.dto.PrizeDistributionRequestEvent;
import com.coupons.prizes.infra.persistence.PrizeDeliveryRepository;
import java.time.Instant;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PrizeDeliveryIngestService {

    private final PrizeDeliveryRepository prizeDeliveryRepository;
    private final PrizeEventMarshaller prizeEventMarshaller;

    public PrizeDeliveryIngestService(
            PrizeDeliveryRepository prizeDeliveryRepository, PrizeEventMarshaller prizeEventMarshaller) {
        this.prizeDeliveryRepository = prizeDeliveryRepository;
        this.prizeEventMarshaller = prizeEventMarshaller;
    }

    @Transactional
    public void processEvent(
            PrizeDistributionRequestEvent event, String correlationId, Integer partition, Long offset) {
        if (prizeDeliveryRepository.existsByCampaignIdAndUserIdAndCouponId(
                event.getCampaignId(), event.getUserId(), event.getCouponId())) {
            return;
        }

        PrizeDelivery delivery = new PrizeDelivery();
        delivery.setCampaignId(event.getCampaignId());
        delivery.setUserId(event.getUserId());
        delivery.setCouponId(event.getCouponId());
        delivery.setCouponCode(event.getCouponCode());
        delivery.setStatus("DELIVERED");
        delivery.setCorrelationId(correlationId);
        delivery.setKafkaPartition(partition);
        delivery.setKafkaOffset(offset);
        delivery.setProcessedAt(Instant.now());
        delivery.setPayloadSnapshot(prizeEventMarshaller.toJson(event));
        prizeDeliveryRepository.save(delivery);
    }
}
