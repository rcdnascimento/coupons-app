package com.coupons.ledger.infra.messaging;

import com.coupons.ledger.domain.service.LedgerCreditService;
import com.coupons.ledger.infra.messaging.dto.DailyChestBonusGrantedMessage;
import com.coupons.ledger.infra.resource.mapper.LedgerRestMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DailyChestBonusKafkaListener {

    private static final Logger log = LoggerFactory.getLogger(DailyChestBonusKafkaListener.class);

    private final LedgerCreditService ledgerCreditService;
    private final LedgerRestMapper ledgerRestMapper;
    private final ObjectMapper objectMapper;

    public DailyChestBonusKafkaListener(
            LedgerCreditService ledgerCreditService,
            LedgerRestMapper ledgerRestMapper,
            ObjectMapper objectMapper) {
        this.ledgerCreditService = ledgerCreditService;
        this.ledgerRestMapper = ledgerRestMapper;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(
            topics = "${coupons.kafka.topic-chest-bonus-granted:chest.bonus.granted}",
            groupId = "${coupons.kafka.consumer-group:ledger-service}")
    @Transactional
    public void onDailyChestBonus(String json) {
        DailyChestBonusGrantedMessage evt;
        try {
            evt = objectMapper.readValue(json, DailyChestBonusGrantedMessage.class);
        } catch (JsonProcessingException e) {
            log.warn("daily chest JSON inválido: {}", e.getMessage());
            return;
        }
        if (evt.getUserId() == null
                || evt.getRewardCoins() < 1
                || evt.getLocalDate() == null
                || evt.getIdempotencyKey() == null
                || evt.getIdempotencyKey().trim().isEmpty()) {
            log.warn("daily chest com campos obrigatórios em falta");
            return;
        }
        
        ledgerCreditService.credit(
                ledgerRestMapper.toDailyChestBonusLine(
                        evt.getUserId(), evt.getRewardCoins(), evt.getIdempotencyKey(), evt.getLocalDate()));
    }
}
