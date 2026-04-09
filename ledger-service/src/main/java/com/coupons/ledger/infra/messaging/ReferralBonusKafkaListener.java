package com.coupons.ledger.infra.messaging;

import com.coupons.ledger.domain.service.LedgerCreditService;
import com.coupons.ledger.infra.messaging.dto.ReferralBonusGrantedMessage;
import com.coupons.ledger.infra.resource.mapper.LedgerRestMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class ReferralBonusKafkaListener {

    private static final Logger log = LoggerFactory.getLogger(ReferralBonusKafkaListener.class);

    private final LedgerCreditService ledgerCreditService;
    private final LedgerRestMapper ledgerRestMapper;
    private final ObjectMapper objectMapper;

    public ReferralBonusKafkaListener(
            LedgerCreditService ledgerCreditService,
            LedgerRestMapper ledgerRestMapper,
            ObjectMapper objectMapper) {
        this.ledgerCreditService = ledgerCreditService;
        this.ledgerRestMapper = ledgerRestMapper;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(
            topics = "${coupons.kafka.topic-referral-bonus-granted:referral.bonus.granted}",
            groupId = "${coupons.kafka.consumer-group:ledger-service}")
    @Transactional
    public void onReferralBonus(String json) {
        ReferralBonusGrantedMessage evt;
        try {
            evt = objectMapper.readValue(json, ReferralBonusGrantedMessage.class);
        } catch (JsonProcessingException e) {
            log.warn("referral.bonus JSON inválido: {}", e.getMessage());
            return;
        }
        if (evt.getNewUserId() == null) {
            log.warn("referral.bonus com newUserId em falta");
            return;
        }
        int amount = evt.getBonusAmount() > 0 ? evt.getBonusAmount() : 10;
        if (evt.getReferrerUserId() == null) {
            String signupId = "signup-bonus:user:" + evt.getNewUserId();
            ledgerCreditService.credit(
                    ledgerRestMapper.toSignupBonusLine(evt.getNewUserId(), amount, signupId));
            return;
        }
        String idNew =
                "referral-bonus:new-user:"
                        + evt.getNewUserId()
                        + ":referrer:"
                        + evt.getReferrerUserId();
        String idRef =
                "referral-bonus:referrer:"
                        + evt.getReferrerUserId()
                        + ":referred:"
                        + evt.getNewUserId();
        ledgerCreditService.credit(
                ledgerRestMapper.toReferralBonusLine(
                        evt.getNewUserId(), amount, idNew, evt.getReferrerUserId()));
        ledgerCreditService.credit(
                ledgerRestMapper.toReferralBonusLine(
                        evt.getReferrerUserId(), amount, idRef, evt.getNewUserId()));
    }
}
