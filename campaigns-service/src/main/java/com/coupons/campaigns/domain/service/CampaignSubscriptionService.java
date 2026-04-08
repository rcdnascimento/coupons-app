package com.coupons.campaigns.domain.service;

import com.coupons.campaigns.domain.CampaignStatus;
import com.coupons.campaigns.domain.CampaignSubscriptionStatus;
import com.coupons.campaigns.domain.entity.Campaign;
import com.coupons.campaigns.domain.entity.CampaignSubscription;
import com.coupons.campaigns.domain.exception.BadRequestException;
import com.coupons.campaigns.infra.resource.dto.MyCampaignSubscriptionResponse;
import com.coupons.campaigns.domain.exception.CampaignNotFoundException;
import com.coupons.campaigns.domain.exception.ConflictException;
import com.coupons.campaigns.infra.messaging.CampaignEventMarshaller;
import com.coupons.campaigns.infra.messaging.dto.CampaignSubscriptionDebitRequestEvent;
import com.coupons.campaigns.infra.persistence.CampaignRepository;
import com.coupons.campaigns.infra.persistence.CampaignSubscriptionRepository;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CampaignSubscriptionService {

    private static final Logger log = LoggerFactory.getLogger(CampaignSubscriptionService.class);

    private final CampaignRepository campaignRepository;
    private final CampaignSubscriptionRepository campaignSubscriptionRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final CampaignEventMarshaller eventMarshaller;
    private final String subscriptionDebitRequestTopic;

    public CampaignSubscriptionService(
            CampaignRepository campaignRepository,
            CampaignSubscriptionRepository campaignSubscriptionRepository,
            KafkaTemplate<String, String> kafkaTemplate,
            CampaignEventMarshaller eventMarshaller,
            @Value(
                    "${coupons.kafka.topic-campaign-subscription-debit-request:campaign.subscription.debit.request}")
                    String subscriptionDebitRequestTopic) {
        this.campaignRepository = campaignRepository;
        this.campaignSubscriptionRepository = campaignSubscriptionRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.eventMarshaller = eventMarshaller;
        this.subscriptionDebitRequestTopic = subscriptionDebitRequestTopic;
    }

    @Transactional
    public void subscribe(UUID campaignId, UUID userId) {
        Campaign campaign =
                campaignRepository.findById(campaignId).orElseThrow(() -> new CampaignNotFoundException(campaignId));
        Instant now = Instant.now();
        if (campaign.getStatus() != CampaignStatus.ACTIVE
                || now.isBefore(campaign.getSubscriptionsStartAt())
                || now.isAfter(campaign.getSubscriptionsEndAt())) {
            throw new BadRequestException("Campanha fora da janela de inscrição");
        }
        Optional<CampaignSubscription> existing = campaignSubscriptionRepository.findByCampaignIdAndUserId(campaignId, userId);
        CampaignSubscription subscription;
        if (existing.isPresent()) {
            CampaignSubscription s = existing.get();
            if (s.getStatus() == CampaignSubscriptionStatus.ACTIVE) {
                throw new ConflictException("Utilizador já inscrito na campanha");
            }
            if (s.getStatus() == CampaignSubscriptionStatus.PROCESSING) {
                throw new ConflictException("Inscrição em processamento; aguarde confirmação do pagamento");
            }
            if (s.getStatus() == CampaignSubscriptionStatus.PAYMENT_FAILED) {
                s.setStatus(CampaignSubscriptionStatus.PROCESSING);
                s.setSubscribedAt(Instant.now());
                subscription = campaignSubscriptionRepository.save(s);
            } else {
                throw new ConflictException("Estado de inscrição inválido");
            }
        } else {
            subscription = new CampaignSubscription();
            subscription.setCampaignId(campaignId);
            subscription.setUserId(userId);
            subscription.setStatus(CampaignSubscriptionStatus.PROCESSING);
            subscription = campaignSubscriptionRepository.save(subscription);
        }

        final UUID subscriptionId = subscription.getId();
        final UUID cid = campaignId;
        final UUID uid = userId;
        final int pointsCost = campaign.getPointsCost();
        final String idempotencyKey = "campaign-subscription-pay:" + subscriptionId;

        CampaignSubscriptionDebitRequestEvent debitEvt = new CampaignSubscriptionDebitRequestEvent();
        debitEvt.setSubscriptionId(subscriptionId);
        debitEvt.setCampaignId(cid);
        debitEvt.setUserId(uid);
        debitEvt.setAmount(pointsCost);
        debitEvt.setIdempotencyKey(idempotencyKey);
        debitEvt.setSchemaVersion(1);
        final String debitPayload = eventMarshaller.toJson(debitEvt);

        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        try {
                            kafkaTemplate
                                    .send(subscriptionDebitRequestTopic, subscriptionId.toString(), debitPayload)
                                    .get();
                        } catch (Exception ex) {
                            log.error(
                                    "Falha ao publicar pedido de débito no Kafka (subscriptionId={})",
                                    subscriptionId,
                                    ex);
                        }
                    }
                });
    }

    @Transactional(readOnly = true)
    public MyCampaignSubscriptionResponse getMySubscriptionStatus(UUID campaignId, UUID userId) {
        if (!campaignRepository.existsById(campaignId)) {
            throw new CampaignNotFoundException(campaignId);
        }
        return campaignSubscriptionRepository
                .findByCampaignIdAndUserId(campaignId, userId)
                .map(s -> new MyCampaignSubscriptionResponse(s.getStatus().name()))
                .orElseGet(() -> new MyCampaignSubscriptionResponse("NONE"));
    }
}
