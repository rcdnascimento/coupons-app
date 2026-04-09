package com.coupons.dailychest.infra.messaging;

import com.coupons.dailychest.infra.messaging.dto.DailyChestBonusGrantedMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class DailyChestBonusKafkaPublisher {

    private static final Logger log = LoggerFactory.getLogger(DailyChestBonusKafkaPublisher.class);

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final String chestBonusGrantedTopic;

    public DailyChestBonusKafkaPublisher(
            KafkaTemplate<String, String> kafkaTemplate,
            ObjectMapper objectMapper,
            @Value("${coupons.kafka.topic-chest-bonus-granted:chest.bonus.granted}") String chestBonusGrantedTopic) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
        this.chestBonusGrantedTopic = chestBonusGrantedTopic;
    }

    public void publish(DailyChestBonusGrantedMessage message) {
        try {
            String payload = objectMapper.writeValueAsString(message);
            kafkaTemplate.send(chestBonusGrantedTopic, message.getUserId().toString(), payload);
        } catch (Exception ex) {
            log.error(
                    "Falha ao publicar evento de bonus diário (userId={}, localDate={})",
                    message.getUserId(),
                    message.getLocalDate(),
                    ex);
        }
    }
}
