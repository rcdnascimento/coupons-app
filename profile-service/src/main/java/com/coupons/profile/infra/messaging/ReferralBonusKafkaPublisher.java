package com.coupons.profile.infra.messaging;

import com.coupons.profile.domain.event.ReferralBonusGrantedEvent;
import com.coupons.profile.infra.messaging.dto.ReferralBonusGrantedMessage;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class ReferralBonusKafkaPublisher {

    private static final Logger log = LoggerFactory.getLogger(ReferralBonusKafkaPublisher.class);

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final String topic;

    public ReferralBonusKafkaPublisher(
            KafkaTemplate<String, String> kafkaTemplate,
            ObjectMapper objectMapper,
            @Value("${coupons.kafka.topic-referral-bonus-granted:referral.bonus.granted}") String topic) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
        this.topic = topic;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onReferralBonusGranted(ReferralBonusGrantedEvent event) {
        ReferralBonusGrantedMessage msg = new ReferralBonusGrantedMessage();
        msg.setNewUserId(event.getNewUserId());
        msg.setReferrerUserId(event.getReferrerUserId());
        msg.setReferralCode(event.getReferralCode());
        msg.setBonusAmount(10);
        msg.setSchemaVersion(1);
        try {
            String payload = objectMapper.writeValueAsString(msg);
            kafkaTemplate.send(topic, event.getNewUserId().toString(), payload);
        } catch (JsonProcessingException e) {
            log.error("Falha ao serializar ReferralBonusGrantedMessage", e);
        }
    }
}
