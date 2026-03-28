package com.coupons.campaigns.infra.messaging;

import com.coupons.campaigns.domain.CampaignSubscriptionStatus;
import com.coupons.campaigns.domain.entity.CampaignSubscription;
import com.coupons.campaigns.infra.messaging.dto.CampaignSubscriptionPaymentFailedEvent;
import com.coupons.campaigns.infra.messaging.dto.CampaignSubscriptionPaymentSucceededEvent;
import com.coupons.campaigns.infra.persistence.CampaignSubscriptionRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class SubscriptionPaymentKafkaListener {

    private static final Logger log = LoggerFactory.getLogger(SubscriptionPaymentKafkaListener.class);

    private final CampaignSubscriptionRepository campaignSubscriptionRepository;
    private final ObjectMapper objectMapper;

    public SubscriptionPaymentKafkaListener(
            CampaignSubscriptionRepository campaignSubscriptionRepository, ObjectMapper objectMapper) {
        this.campaignSubscriptionRepository = campaignSubscriptionRepository;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(
            topics = "${coupons.kafka.topic-campaign-subscription-payment-succeeded:campaign.subscription.payment.succeeded}",
            groupId = "${coupons.kafka.consumer-group:campaigns-service}")
    @Transactional
    public void onPaymentSucceeded(String json) {
        CampaignSubscriptionPaymentSucceededEvent evt;
        try {
            evt = objectMapper.readValue(json, CampaignSubscriptionPaymentSucceededEvent.class);
        } catch (JsonProcessingException e) {
            log.warn("payment.succeeded JSON inválido: {}", e.getMessage());
            return;
        }
        if (evt.getSubscriptionId() == null) {
            return;
        }
        Optional<CampaignSubscription> opt = campaignSubscriptionRepository.findById(evt.getSubscriptionId());
        if (opt.isEmpty()) {
            log.warn("Subscrição {} não encontrada para payment.succeeded", evt.getSubscriptionId());
            return;
        }
        CampaignSubscription sub = opt.get();
        if (sub.getStatus() != CampaignSubscriptionStatus.PROCESSING) {
            return;
        }
        if (!sub.getCampaignId().equals(evt.getCampaignId()) || !sub.getUserId().equals(evt.getUserId())) {
            log.warn("Correlação inconsistente em payment.succeeded (subscriptionId={})", evt.getSubscriptionId());
            return;
        }
        sub.setStatus(CampaignSubscriptionStatus.ACTIVE);
        campaignSubscriptionRepository.save(sub);
    }

    @KafkaListener(
            topics = "${coupons.kafka.topic-campaign-subscription-payment-failed:campaign.subscription.payment.failed}",
            groupId = "${coupons.kafka.consumer-group:campaigns-service}")
    @Transactional
    public void onPaymentFailed(String json) {
        CampaignSubscriptionPaymentFailedEvent evt;
        try {
            evt = objectMapper.readValue(json, CampaignSubscriptionPaymentFailedEvent.class);
        } catch (JsonProcessingException e) {
            log.warn("payment.failed JSON inválido: {}", e.getMessage());
            return;
        }
        if (evt.getSubscriptionId() == null) {
            return;
        }
        Optional<CampaignSubscription> opt = campaignSubscriptionRepository.findById(evt.getSubscriptionId());
        if (opt.isEmpty()) {
            log.warn("Subscrição {} não encontrada para payment.failed", evt.getSubscriptionId());
            return;
        }
        CampaignSubscription sub = opt.get();
        if (sub.getStatus() != CampaignSubscriptionStatus.PROCESSING) {
            return;
        }
        if (!sub.getCampaignId().equals(evt.getCampaignId()) || !sub.getUserId().equals(evt.getUserId())) {
            log.warn("Correlação inconsistente em payment.failed (subscriptionId={})", evt.getSubscriptionId());
            return;
        }
        sub.setStatus(CampaignSubscriptionStatus.PAYMENT_FAILED);
        campaignSubscriptionRepository.save(sub);
    }
}
