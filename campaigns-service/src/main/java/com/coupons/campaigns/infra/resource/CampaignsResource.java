package com.coupons.campaigns.infra.resource;

import com.coupons.campaigns.domain.service.CampaignAllocationService;
import com.coupons.campaigns.domain.service.CampaignCouponService;
import com.coupons.campaigns.domain.service.CampaignManagementService;
import com.coupons.campaigns.domain.service.CampaignSubscriptionService;
import com.coupons.campaigns.infra.resource.dto.AddCouponToCampaignRequest;
import com.coupons.campaigns.infra.resource.dto.AllocationResponse;
import com.coupons.campaigns.infra.resource.dto.CampaignResponse;
import com.coupons.campaigns.infra.resource.dto.CreateCampaignRequest;
import com.coupons.campaigns.infra.resource.dto.UserIdRequest;
import com.coupons.campaigns.infra.resource.mapper.CampaignRestMapper;
import java.util.List;
import java.util.UUID;
import javax.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/campaigns")
public class CampaignsResource {

    private final CampaignManagementService campaignManagementService;
    private final CampaignCouponService campaignCouponService;
    private final CampaignSubscriptionService campaignSubscriptionService;
    private final CampaignAllocationService campaignAllocationService;
    private final CampaignRestMapper campaignRestMapper;

    public CampaignsResource(
            CampaignManagementService campaignManagementService,
            CampaignCouponService campaignCouponService,
            CampaignSubscriptionService campaignSubscriptionService,
            CampaignAllocationService campaignAllocationService,
            CampaignRestMapper campaignRestMapper) {
        this.campaignManagementService = campaignManagementService;
        this.campaignCouponService = campaignCouponService;
        this.campaignSubscriptionService = campaignSubscriptionService;
        this.campaignAllocationService = campaignAllocationService;
        this.campaignRestMapper = campaignRestMapper;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CampaignResponse create(@Valid @RequestBody CreateCampaignRequest request) {
        return campaignRestMapper.toResponse(
                campaignManagementService.createCampaign(campaignRestMapper.toCampaign(request)));
    }

    @GetMapping
    public List<CampaignResponse> list() {
        return campaignRestMapper.toCampaignResponseList(campaignManagementService.listCampaigns());
    }

    @PostMapping("/{campaignId}/coupons")
    public CampaignResponse addCoupon(
            @PathVariable UUID campaignId, @Valid @RequestBody AddCouponToCampaignRequest request) {
        return campaignRestMapper.toResponse(
                campaignCouponService.addCoupon(
                        campaignId, campaignRestMapper.toCouponDraft(request), request.getPriority()));
    }

    @PostMapping("/{campaignId}/subscriptions")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void subscribe(@PathVariable UUID campaignId, @Valid @RequestBody UserIdRequest request) {
        campaignSubscriptionService.subscribe(campaignId, request.getUserId());
    }

    @PostMapping("/{campaignId}/allocations")
    public AllocationResponse allocate(@PathVariable UUID campaignId, @Valid @RequestBody UserIdRequest request) {
        return campaignRestMapper.toResponse(
                campaignAllocationService.allocate(campaignId, request.getUserId()));
    }
}
