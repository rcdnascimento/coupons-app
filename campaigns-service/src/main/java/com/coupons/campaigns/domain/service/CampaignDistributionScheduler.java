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
        List<Campaign> dueCampaigns =
                campaignRepository.findByStatusAndDistributionAtLessThanEqualOrderByDistributionAtAsc(
                        CampaignStatus.ACTIVE, Instant.now());
        for (Campaign campaign : dueCampaigns) {
            List<CampaignSubscription> subscriptions =
                    campaignSubscriptionRepository.findByCampaignIdAndStatusOrderBySubscribedAtAsc(
                            campaign.getId(), CampaignSubscriptionStatus.ACTIVE);
            boolean allAllocated = true;
            for (CampaignSubscription subscription : subscriptions) {
                if (!campaignAllocationRepository.existsByCampaignIdAndUserId(
                        campaign.getId(), subscription.getUserId())) {
                    try {
                        campaignAllocationService.allocate(campaign.getId(), subscription.getUserId());
                    } catch (ConflictException ex) {
                        // Sem cupons suficientes ou já alocado; segue para próximos utilizadores/campanhas.
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

            if (allAllocated) {
                campaign.setStatus(CampaignStatus.CLOSED);
                campaignRepository.save(campaign);
            }
        }
    }
}
