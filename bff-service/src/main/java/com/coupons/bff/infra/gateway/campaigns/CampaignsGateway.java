package com.coupons.bff.infra.gateway.campaigns;

import com.coupons.bff.infra.resource.dto.AddCouponToCampaignRequest;
import com.coupons.bff.infra.resource.dto.CampaignResponse;
import com.coupons.bff.infra.resource.dto.CampaignSummaryResponse;
import com.coupons.bff.infra.resource.dto.CampaignWinnersResponse;
import com.coupons.bff.infra.resource.dto.CompanyResponse;
import com.coupons.bff.infra.resource.dto.CreateCompanyRequest;
import com.coupons.bff.infra.resource.dto.CreateCampaignRequest;
import com.coupons.bff.infra.resource.dto.MyCampaignSubscriptionResponse;
import com.coupons.bff.infra.resource.dto.PatchCampaignRequest;
import com.coupons.bff.infra.resource.dto.UserIdRequest;
import java.util.List;

public interface CampaignsGateway {

    CampaignResponse createCampaign(CreateCampaignRequest request);
    CompanyResponse createCompany(CreateCompanyRequest request);

    List<CampaignResponse> listCampaigns();
    List<CompanyResponse> listCompanies();

    CampaignResponse getCampaign(String campaignId);

    CampaignResponse patchCampaign(String campaignId, PatchCampaignRequest request);

    CampaignResponse addCoupon(String campaignId, AddCouponToCampaignRequest request);

    void subscribe(String campaignId, UserIdRequest request);

    MyCampaignSubscriptionResponse mySubscription(String campaignId, String userId);

    CampaignSummaryResponse summary(String campaignId);

    CampaignWinnersResponse winners(String campaignId);
}
