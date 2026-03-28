package com.coupons.campaigns.infra.resource.mapper;

import com.coupons.campaigns.domain.entity.Campaign;
import com.coupons.campaigns.domain.entity.CampaignAllocation;
import com.coupons.campaigns.domain.entity.Coupon;
import com.coupons.campaigns.infra.resource.dto.AddCouponToCampaignRequest;
import com.coupons.campaigns.infra.resource.dto.AllocationResponse;
import com.coupons.campaigns.infra.resource.dto.CampaignResponse;
import com.coupons.campaigns.infra.resource.dto.CreateCampaignRequest;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class CampaignRestMapper {

    public Campaign toCampaign(CreateCampaignRequest request) {
        Campaign campaign = new Campaign();
        campaign.setTitle(request.getTitle().trim());
        campaign.setSubscriptionsStartAt(request.getSubscriptionsStartAt());
        campaign.setSubscriptionsEndAt(request.getSubscriptionsEndAt());
        campaign.setDistributionAt(request.getDistributionAt());
        campaign.setPointsCost(request.getPointsCost());
        return campaign;
    }

    public Coupon toCouponDraft(AddCouponToCampaignRequest request) {
        Coupon coupon = new Coupon();
        coupon.setCode(request.getCode().trim());
        coupon.setExpiresAt(request.getExpiresAt());
        return coupon;
    }

    public CampaignResponse toResponse(Campaign campaign) {
        return CampaignResponse.from(campaign);
    }

    public List<CampaignResponse> toCampaignResponseList(List<Campaign> campaigns) {
        return campaigns.stream().map(CampaignResponse::from).collect(Collectors.toList());
    }

    public AllocationResponse toResponse(CampaignAllocation allocation) {
        return AllocationResponse.from(allocation);
    }
}
