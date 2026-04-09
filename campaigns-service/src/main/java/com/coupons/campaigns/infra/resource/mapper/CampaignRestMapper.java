package com.coupons.campaigns.infra.resource.mapper;

import com.coupons.campaigns.domain.entity.Campaign;
import com.coupons.campaigns.domain.entity.CampaignAllocation;
import com.coupons.campaigns.domain.entity.Company;
import com.coupons.campaigns.domain.entity.Coupon;
import com.coupons.campaigns.infra.persistence.CompanyRepository;
import com.coupons.campaigns.infra.resource.dto.AddCouponToCampaignRequest;
import com.coupons.campaigns.infra.resource.dto.AllocationResponse;
import com.coupons.campaigns.infra.resource.dto.CampaignResponse;
import com.coupons.campaigns.infra.resource.dto.CreateCampaignRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class CampaignRestMapper {

    private final CompanyRepository companyRepository;

    public CampaignRestMapper(CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    public Campaign toCampaign(CreateCampaignRequest request) {
        Campaign campaign = new Campaign();
        campaign.setTitle(request.getTitle().trim());
        campaign.setDescription(request.getDescription().trim());
        campaign.setSubscriptionsStartAt(request.getSubscriptionsStartAt());
        campaign.setSubscriptionsEndAt(request.getSubscriptionsEndAt());
        campaign.setDistributionAt(request.getDistributionAt());
        campaign.setCompanyId(request.getCompanyId());
        campaign.setImageUrl(blankToNull(request.getImageUrl()));
        campaign.setVisibleUntil(request.getVisibleUntil());
        campaign.setPointsCost(request.getPointsCost());
        return campaign;
    }

    public Coupon toCouponDraft(AddCouponToCampaignRequest request) {
        Coupon coupon = new Coupon();
        coupon.setCode(request.getCode().trim());
        if (request.getTitle() != null && !request.getTitle().isBlank()) {
            coupon.setTitle(request.getTitle().trim());
        }
        return coupon;
    }

    public CampaignResponse toResponse(Campaign campaign) {
        CampaignResponse r = CampaignResponse.from(campaign);
        enrichCompany(r, campaign.getCompanyId());
        return r;
    }

    public List<CampaignResponse> toCampaignResponseList(List<Campaign> campaigns) {
        Set<UUID> companyIds =
                campaigns.stream()
                        .map(Campaign::getCompanyId)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toSet());
        Map<UUID, Company> companyById = new HashMap<>();
        for (UUID cid : companyIds) {
            companyRepository.findById(cid).ifPresent(co -> companyById.put(cid, co));
        }
        return campaigns.stream()
                .map(
                        c -> {
                            CampaignResponse r = CampaignResponse.from(c);
                            if (c.getCompanyId() != null) {
                                Company co = companyById.get(c.getCompanyId());
                                if (co != null) {
                                    r.setCompanyName(co.getName());
                                    r.setCompanyLogoUrl(co.getLogoUrl());
                                }
                            }
                            return r;
                        })
                .collect(Collectors.toList());
    }

    public AllocationResponse toResponse(CampaignAllocation allocation) {
        return AllocationResponse.from(allocation);
    }

    private String blankToNull(String v) {
        if (v == null) return null;
        String t = v.trim();
        return t.isEmpty() ? null : t;
    }

    private void enrichCompany(CampaignResponse r, UUID companyId) {
        if (companyId == null) {
            return;
        }
        companyRepository
                .findById(companyId)
                .ifPresent(
                        co -> {
                            r.setCompanyName(co.getName());
                            r.setCompanyLogoUrl(co.getLogoUrl());
                        });
    }
}
