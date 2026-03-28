package com.coupons.prizes.infra.messaging;

import com.coupons.prizes.domain.service.PrizeDeliveryIngestService;
import com.coupons.prizes.infra.messaging.dto.PrizeDistributionRequestEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Component
public class PrizeDistributionConsumer {

    private final PrizeDeliveryIngestService prizeDeliveryIngestService;
    private final ObjectMapper objectMapper;

    public PrizeDistributionConsumer(
            PrizeDeliveryIngestService prizeDeliveryIngestService, ObjectMapper objectMapper) {
        this.prizeDeliveryIngestService = prizeDeliveryIngestService;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(
            topics = "${coupons.kafka.topic-prize-distribution-request:prize.distribution.request}",
            groupId = "${coupons.kafka.consumer-group:prizes-service}")
    public void consume(
            String payload,
            @Header(value = KafkaHeaders.RECEIVED_MESSAGE_KEY, required = false) String key,
            @Header(KafkaHeaders.RECEIVED_PARTITION_ID) int partition,
            @Header(KafkaHeaders.OFFSET) long offset) {
        try {
            PrizeDistributionRequestEvent event =
                    objectMapper.readValue(payload, PrizeDistributionRequestEvent.class);
            prizeDeliveryIngestService.processEvent(event, key, partition, offset);
        } catch (Exception ex) {
            throw new IllegalStateException("Falha ao processar evento de prêmio", ex);
        }
    }
}

