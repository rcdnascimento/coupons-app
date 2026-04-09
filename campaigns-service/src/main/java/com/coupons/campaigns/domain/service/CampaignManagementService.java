package com.coupons.campaigns.domain.service;

import com.coupons.campaigns.domain.CampaignStatus;
import com.coupons.campaigns.domain.entity.Campaign;
import com.coupons.campaigns.domain.exception.BadRequestException;
import com.coupons.campaigns.domain.exception.CampaignNotFoundException;
import com.coupons.campaigns.infra.persistence.CompanyRepository;
import com.coupons.campaigns.infra.persistence.CampaignRepository;
import com.coupons.campaigns.infra.resource.dto.PatchCampaignRequest;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CampaignManagementService {

    private final CampaignRepository campaignRepository;
    private final CompanyRepository companyRepository;

    public CampaignManagementService(CampaignRepository campaignRepository, CompanyRepository companyRepository) {
        this.campaignRepository = campaignRepository;
        this.companyRepository = companyRepository;
    }

    @Transactional
    public Campaign createCampaign(Campaign campaign) {
        validateCompanyIfPresent(campaign.getCompanyId());
        validateTimeline(campaign);
        campaign.setTitle(campaign.getTitle().trim());
        if (campaign.getDescription() != null) {
            campaign.setDescription(campaign.getDescription().trim());
        }
        campaign.setStatus(CampaignStatus.ACTIVE);
        return campaignRepository.save(campaign);
    }

    @Transactional(readOnly = true)
    public Campaign getById(UUID id) {
        return campaignRepository.findById(id).orElseThrow(() -> new CampaignNotFoundException(id));
    }

    @Transactional
    public Campaign patchCampaign(UUID id, PatchCampaignRequest patch) {
        Campaign campaign = getById(id);
        if (patch.getTitle() != null) {
            campaign.setTitle(patch.getTitle().trim());
        }
        if (patch.getDescription() != null) {
            campaign.setDescription(patch.getDescription().trim());
        }
        if (patch.getSubscriptionsStartAt() != null) {
            campaign.setSubscriptionsStartAt(patch.getSubscriptionsStartAt());
        }
        if (patch.getSubscriptionsEndAt() != null) {
            campaign.setSubscriptionsEndAt(patch.getSubscriptionsEndAt());
        }
        if (patch.getDistributionAt() != null) {
            campaign.setDistributionAt(patch.getDistributionAt());
        }
        if (Boolean.TRUE.equals(patch.getClearCompany())) {
            campaign.setCompanyId(null);
        } else if (patch.getCompanyId() != null) {
            validateCompanyIfPresent(patch.getCompanyId());
            campaign.setCompanyId(patch.getCompanyId());
        }
        if (Boolean.TRUE.equals(patch.getClearImageUrl())) {
            campaign.setImageUrl(null);
        } else if (patch.getImageUrl() != null) {
            campaign.setImageUrl(blankToNull(patch.getImageUrl()));
        }
        if (Boolean.TRUE.equals(patch.getClearVisibleUntil())) {
            campaign.setVisibleUntil(null);
        } else if (patch.getVisibleUntil() != null) {
            campaign.setVisibleUntil(patch.getVisibleUntil());
        }
        if (patch.getPointsCost() != null) {
            campaign.setPointsCost(patch.getPointsCost());
        }
        if (patch.getStatus() != null) {
            campaign.setStatus(patch.getStatus());
        }
        validateTimeline(campaign);
        return campaignRepository.save(campaign);
    }

    @Transactional(readOnly = true)
    public List<Campaign> listCampaigns() {
        return campaignRepository.findAllForListOrderByActiveAndDistribution();
    }

    private void validateTimeline(Campaign campaign) {
        if (!campaign.getSubscriptionsStartAt().isBefore(campaign.getSubscriptionsEndAt())) {
            throw new BadRequestException("subscriptionsStartAt deve ser anterior a subscriptionsEndAt");
        }
        if (campaign.getDistributionAt().isBefore(campaign.getSubscriptionsEndAt())) {
            throw new BadRequestException("distributionAt deve ser posterior ao fim das inscrições");
        }
    }

    private void validateCompanyIfPresent(UUID companyId) {
        if (companyId == null) return;
        if (!companyRepository.existsById(companyId)) {
            throw new BadRequestException("companyId inválido: empresa não encontrada");
        }
    }

    private String blankToNull(String v) {
        if (v == null) return null;
        String t = v.trim();
        return t.isEmpty() ? null : t;
    }
}
