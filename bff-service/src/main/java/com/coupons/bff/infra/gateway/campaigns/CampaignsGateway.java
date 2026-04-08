package com.coupons.bff.infra.gateway.campaigns;

import com.coupons.bff.infra.resource.dto.AddCouponToCampaignRequest;
import com.coupons.bff.infra.resource.dto.CampaignResponse;
import com.coupons.bff.infra.resource.dto.CampaignSummaryResponse;
import com.coupons.bff.infra.resource.dto.CampaignWinnersResponse;
import com.coupons.bff.infra.resource.dto.CreateCampaignRequest;
import com.coupons.bff.infra.resource.dto.UserIdRequest;
import java.util.List;

public interface CampaignsGateway {

    CampaignResponse createCampaign(CreateCampaignRequest request);

    List<CampaignResponse> listCampaigns();

    CampaignResponse addCoupon(String campaignId, AddCouponToCampaignRequest request);

    void subscribe(String campaignId, UserIdRequest request);

    CampaignSummaryResponse summary(String campaignId);

    CampaignWinnersResponse winners(String campaignId);
}
