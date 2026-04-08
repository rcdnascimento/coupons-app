package com.coupons.campaigns.domain.service;

import com.coupons.campaigns.domain.CampaignStatus;
import com.coupons.campaigns.domain.CampaignSubscriptionStatus;
import com.coupons.campaigns.domain.entity.Campaign;
import com.coupons.campaigns.domain.entity.CampaignSubscription;
import com.coupons.campaigns.domain.exception.ConflictException;
import com.coupons.campaigns.infra.persistence.CampaignAllocationRepository;
import com.coupons.campaigns.infra.persistence.CampaignRepository;
import com.coupons.campaigns.infra.persistence.CampaignSubscriptionRepository;
import java.time.Instant;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class CampaignDistributionScheduler {

    private static final Logger log = LoggerFactory.getLogger(CampaignDistributionScheduler.class);

    private final CampaignRepository campaignRepository;
    private final CampaignSubscriptionRepository campaignSubscriptionRepository;
    private final CampaignAllocationRepository campaignAllocationRepository;
    private final CampaignAllocationService campaignAllocationService;

    public CampaignDistributionScheduler(
            CampaignRepository campaignRepository,
            CampaignSubscriptionRepository campaignSubscriptionRepository,
            CampaignAllocationRepository campaignAllocationRepository,
            CampaignAllocationService campaignAllocationService) {
        this.campaignRepository = campaignRepository;
        this.campaignSubscriptionRepository = campaignSubscriptionRepository;
        this.campaignAllocationRepository = campaignAllocationRepository;
        this.campaignAllocationService = campaignAllocationService;
    }

    @Scheduled(fixedDelayString = "${coupons.distribution.poll-ms:5000}")
    @Transactional
    public void runScheduledDistribution() {
        Instant now = Instant.now();
        List<Campaign> dueCampaigns =
                campaignRepository.findByStatusAndDistributionAtLessThanEqualOrderByDistributionAtAsc(
                        CampaignStatus.ACTIVE, now);
        if (dueCampaigns.isEmpty()) {
            return;
        }

        log.info(
                "Distribuição: {} campanha(s) ACTIVE com distributionAt <= now ({}); a processar alocações e fecho.",
                dueCampaigns.size(),
                now);

        for (Campaign campaign : dueCampaigns) {
            List<CampaignSubscription> subscriptions =
                    campaignSubscriptionRepository.findByCampaignIdAndStatusOrderBySubscribedAtAsc(
                            campaign.getId(), CampaignSubscriptionStatus.ACTIVE);
            int subCount = subscriptions.size();
            long allocationsBefore = campaignAllocationRepository.countByCampaignId(campaign.getId());

            log.info(
                    "Campanha em distribuição: campaignId={}, title={}, distributionAt={}, inscricoesACTIVE={}, alocacoesExistentes={}",
                    campaign.getId(),
                    campaign.getTitle(),
                    campaign.getDistributionAt(),
                    subCount,
                    allocationsBefore);

            boolean allAllocated = true;
            for (CampaignSubscription subscription : subscriptions) {
                if (!campaignAllocationRepository.existsByCampaignIdAndUserId(
                        campaign.getId(), subscription.getUserId())) {
                    try {
                        campaignAllocationService.allocate(campaign.getId(), subscription.getUserId());
                    } catch (ConflictException ex) {
                        log.debug(
                                "Alocação não efetuada (campaignId={}, userId={}): {}",
                                campaign.getId(),
                                subscription.getUserId(),
                                ex.getMessage());
                    } catch (RuntimeException ex) {
                        log.warn(
                                "Falha ao alocar/publicar prêmio (campaignId={}, userId={}): {}",
                                campaign.getId(),
                                subscription.getUserId(),
                                ex.getMessage());
                    }
                    if (!campaignAllocationRepository.existsByCampaignIdAndUserId(
                            campaign.getId(), subscription.getUserId())) {
                        allAllocated = false;
                    }
                }
            }

            long allocationsAfter = campaignAllocationRepository.countByCampaignId(campaign.getId());

            if (allAllocated) {
                campaign.setStatus(CampaignStatus.CLOSED);
                campaignRepository.save(campaign);
                log.info(
                        "Campanha CLOSED após distribuição: campaignId={}, title={}, alocacoes={}, inscricoesACTIVE={} (todos com prémio alocado ou sem inscrições).",
                        campaign.getId(),
                        campaign.getTitle(),
                        allocationsAfter,
                        subCount);
            } else {
                log.warn(
                        "Campanha mantém-se ACTIVE: ainda faltam alocações (campaignId={}, title={}, alocacoes={}, inscricoesACTIVE={}). Verifique cupons ligados ao pool.",
                        campaign.getId(),
                        campaign.getTitle(),
                        allocationsAfter,
                        subCount);
            }
        }
    }
}
