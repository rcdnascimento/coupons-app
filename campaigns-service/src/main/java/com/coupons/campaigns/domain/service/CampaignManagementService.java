package com.coupons.campaigns.domain.service;

import com.coupons.campaigns.domain.CampaignStatus;
import com.coupons.campaigns.domain.entity.Campaign;
import com.coupons.campaigns.domain.exception.BadRequestException;
import com.coupons.campaigns.infra.persistence.CampaignRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CampaignManagementService {

    private final CampaignRepository campaignRepository;

    public CampaignManagementService(CampaignRepository campaignRepository) {
        this.campaignRepository = campaignRepository;
    }

    @Transactional
    public Campaign createCampaign(Campaign campaign) {
        if (!campaign.getSubscriptionsStartAt().isBefore(campaign.getSubscriptionsEndAt())) {
            throw new BadRequestException("subscriptionsStartAt deve ser anterior a subscriptionsEndAt");
        }
        if (campaign.getDistributionAt().isBefore(campaign.getSubscriptionsEndAt())) {
            throw new BadRequestException("distributionAt deve ser posterior ao fim das inscrições");
        }

        campaign.setTitle(campaign.getTitle().trim());
        campaign.setStatus(CampaignStatus.ACTIVE);
        return campaignRepository.save(campaign);
    }

    @Transactional(readOnly = true)
    public List<Campaign> listCampaigns() {
        return campaignRepository.findAll();
    }
}
