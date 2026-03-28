package com.coupons.ledger.infra.messaging;

import com.coupons.ledger.domain.entity.LedgerEntry;
import com.coupons.ledger.domain.exception.InsufficientBalanceException;
import com.coupons.ledger.domain.service.LedgerDebitService;
import com.coupons.ledger.infra.messaging.dto.CampaignSubscriptionDebitRequestEvent;
import com.coupons.ledger.infra.messaging.dto.CampaignSubscriptionPaymentFailedEvent;
import com.coupons.ledger.infra.messaging.dto.CampaignSubscriptionPaymentSucceededEvent;
import com.coupons.ledger.infra.persistence.LedgerEntryRepository;
import com.coupons.ledger.infra.resource.mapper.LedgerRestMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class CampaignSubscriptionDebitKafkaListener {

    private static final Logger log = LoggerFactory.getLogger(CampaignSubscriptionDebitKafkaListener.class);

    private final LedgerDebitService ledgerDebitService;
    private final LedgerEntryRepository ledgerEntryRepository;
    private final LedgerRestMapper ledgerRestMapper;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final String paymentSucceededTopic;
    private final String paymentFailedTopic;

    public CampaignSubscriptionDebitKafkaListener(
            LedgerDebitService ledgerDebitService,
            LedgerEntryRepository ledgerEntryRepository,
            LedgerRestMapper ledgerRestMapper,
            KafkaTemplate<String, String> kafkaTemplate,
            ObjectMapper objectMapper,
            @Value("${coupons.kafka.topic-campaign-subscription-payment-succeeded:campaign.subscription.payment.succeeded}")
                    String paymentSucceededTopic,
            @Value("${coupons.kafka.topic-campaign-subscription-payment-failed:campaign.subscription.payment.failed}")
                    String paymentFailedTopic) {
        this.ledgerDebitService = ledgerDebitService;
        this.ledgerEntryRepository = ledgerEntryRepository;
        this.ledgerRestMapper = ledgerRestMapper;
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
        this.paymentSucceededTopic = paymentSucceededTopic;
        this.paymentFailedTopic = paymentFailedTopic;
    }

    @KafkaListener(
            topics = "${coupons.kafka.topic-campaign-subscription-debit-request:campaign.subscription.debit.request}",
            groupId = "${coupons.kafka.consumer-group:ledger-service}")
    public void onDebitRequest(String json) {
        CampaignSubscriptionDebitRequestEvent evt;
        try {
            evt = objectMapper.readValue(json, CampaignSubscriptionDebitRequestEvent.class);
        } catch (JsonProcessingException e) {
            log.warn("Mensagem de débito de subscrição inválida: {}", e.getMessage());
            return;
        }
        if (evt.getSubscriptionId() == null
                || evt.getUserId() == null
                || evt.getCampaignId() == null
                || evt.getIdempotencyKey() == null
                || evt.getIdempotencyKey().isBlank()) {
            log.warn("Pedido de débito com campos obrigatórios em falta");
            return;
        }
        if (evt.getAmount() < 1) {
            publishFailed(evt, "Valor de débito inválido");
            return;
        }

        Optional<LedgerEntry> existing = ledgerEntryRepository.findByIdempotencyKey(evt.getIdempotencyKey().trim());
        if (existing.isPresent()) {
            publishSucceeded(evt, existing.get().getId());
            return;
        }

        LedgerEntry line =
                ledgerRestMapper.toLedgerLineFromSubscriptionDebit(
                        evt.getUserId(), evt.getAmount(), evt.getIdempotencyKey(), evt.getCampaignId());

        try {
            LedgerEntry saved = ledgerDebitService.debit(line);
            publishSucceeded(evt, saved.getId());
        } catch (InsufficientBalanceException ex) {
            publishFailed(evt, "Saldo insuficiente para inscrição na campanha");
        } catch (Exception ex) {
            log.error("Erro ao processar débito de subscrição (subscriptionId={})", evt.getSubscriptionId(), ex);
            publishFailed(evt, ex.getMessage() != null ? ex.getMessage() : "Erro no ledger");
        }
    }

    private void publishSucceeded(CampaignSubscriptionDebitRequestEvent evt, UUID ledgerEntryId) {
        CampaignSubscriptionPaymentSucceededEvent out = new CampaignSubscriptionPaymentSucceededEvent();
        out.setSubscriptionId(evt.getSubscriptionId());
        out.setCampaignId(evt.getCampaignId());
        out.setUserId(evt.getUserId());
        out.setLedgerEntryId(ledgerEntryId);
        try {
            String payload = objectMapper.writeValueAsString(out);
            kafkaTemplate.send(paymentSucceededTopic, evt.getSubscriptionId().toString(), payload);
        } catch (Exception e) {
            log.error("Falha ao publicar payment.succeeded (subscriptionId={})", evt.getSubscriptionId(), e);
        }
    }

    private void publishFailed(CampaignSubscriptionDebitRequestEvent evt, String message) {
        CampaignSubscriptionPaymentFailedEvent out = new CampaignSubscriptionPaymentFailedEvent();
        out.setSubscriptionId(evt.getSubscriptionId());
        out.setCampaignId(evt.getCampaignId());
        out.setUserId(evt.getUserId());
        out.setError(message);
        try {
            String payload = objectMapper.writeValueAsString(out);
            kafkaTemplate.send(paymentFailedTopic, evt.getSubscriptionId().toString(), payload);
        } catch (Exception e) {
            log.error("Falha ao publicar payment.failed (subscriptionId={})", evt.getSubscriptionId(), e);
        }
    }
}
