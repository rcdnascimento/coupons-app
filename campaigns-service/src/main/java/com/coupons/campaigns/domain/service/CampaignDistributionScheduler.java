package com.coupons.campaigns.domain.service;

import com.coupons.campaigns.domain.CampaignStatus;
import com.coupons.campaigns.domain.CampaignSubscriptionStatus;
import com.coupons.campaigns.domain.entity.Campaign;
import com.coupons.campaigns.domain.entity.CampaignSubscription;
import com.coupons.campaigns.domain.exception.ConflictException;
import com.coupons.campaigns.infra.persistence.CampaignAllocationRepository;
import com.coupons.campaigns.infra.persistence.CampaignCouponRepository;
import com.coupons.campaigns.infra.persistence.CampaignRepository;
import com.coupons.campaigns.infra.persistence.CampaignSubscriptionRepository;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
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
    private final CampaignCouponRepository campaignCouponRepository;
    private final CampaignAllocationService campaignAllocationService;

    public CampaignDistributionScheduler(
            CampaignRepository campaignRepository,
            CampaignSubscriptionRepository campaignSubscriptionRepository,
            CampaignAllocationRepository campaignAllocationRepository,
            CampaignCouponRepository campaignCouponRepository,
            CampaignAllocationService campaignAllocationService) {
        this.campaignRepository = campaignRepository;
        this.campaignSubscriptionRepository = campaignSubscriptionRepository;
        this.campaignAllocationRepository = campaignAllocationRepository;
        this.campaignCouponRepository = campaignCouponRepository;
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
                    new ArrayList<>(
                            campaignSubscriptionRepository.findByCampaignIdAndStatusOrderBySubscribedAtAsc(
                                    campaign.getId(), CampaignSubscriptionStatus.ACTIVE));
            Collections.shuffle(subscriptions, ThreadLocalRandom.current());
            int subCount = subscriptions.size();
            long allocationsBefore = campaignAllocationRepository.countByCampaignId(campaign.getId());

            log.info(
                    "Campanha em distribuição: campaignId={}, title={}, distributionAt={}, inscricoesACTIVE={}, alocacoesExistentes={}",
                    campaign.getId(),
                    campaign.getTitle(),
                    campaign.getDistributionAt(),
                    subCount,
                    allocationsBefore);

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
                }
            }

            long allocationsAfter = campaignAllocationRepository.countByCampaignId(campaign.getId());
            long couponLinks = campaignCouponRepository.countByCampaignId(campaign.getId());
            long maxPrizes = Math.min(couponLinks, subCount);

            boolean shouldClose = subCount == 0 || (maxPrizes > 0 && allocationsAfter >= maxPrizes);

            if (shouldClose) {
                campaign.setStatus(CampaignStatus.CLOSED);
                campaignRepository.save(campaign);
                log.info(
                        "Campanha CLOSED: campaignId={}, title={}, alocacoes={}, cuponsLigados={}, inscricoesACTIVE={}, limitePrémios={}.",
                        campaign.getId(),
                        campaign.getTitle(),
                        allocationsAfter,
                        couponLinks,
                        subCount,
                        maxPrizes);
            } else {
                log.warn(
                        "Campanha mantém-se ACTIVE: alocacoes={}/{} (cupons={}, inscricoes={}) campaignId={}, title={}.",
                        allocationsAfter,
                        maxPrizes,
                        couponLinks,
                        subCount,
                        campaign.getId(),
                        campaign.getTitle());
            }
        }
    }
}
