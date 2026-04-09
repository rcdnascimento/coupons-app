package com.coupons.campaigns.infra.resource;

import com.coupons.campaigns.domain.service.CampaignAllocationService;
import com.coupons.campaigns.domain.service.CampaignCouponService;
import com.coupons.campaigns.domain.service.CampaignManagementService;
import com.coupons.campaigns.domain.service.CampaignSummaryService;
import com.coupons.campaigns.domain.service.CampaignSubscriptionService;
import com.coupons.campaigns.domain.service.CampaignWinnersService;
import com.coupons.campaigns.infra.resource.dto.AddCouponToCampaignRequest;
import com.coupons.campaigns.infra.resource.dto.AllocationResponse;
import com.coupons.campaigns.infra.resource.dto.CampaignCouponLinkResponse;
import com.coupons.campaigns.infra.resource.dto.CampaignResponse;
import com.coupons.campaigns.infra.resource.dto.CampaignSummaryResponse;
import com.coupons.campaigns.infra.resource.dto.CampaignWinnersResponse;
import com.coupons.campaigns.infra.resource.dto.CreateCampaignRequest;
import com.coupons.campaigns.infra.resource.dto.MyCampaignSubscriptionResponse;
import com.coupons.campaigns.infra.resource.dto.PatchCampaignRequest;
import com.coupons.campaigns.infra.resource.dto.UserIdRequest;
import com.coupons.campaigns.infra.resource.mapper.CampaignRestMapper;
import java.util.List;
import java.util.UUID;
import javax.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/campaigns")
public class CampaignsResource {

    private final CampaignManagementService campaignManagementService;
    private final CampaignCouponService campaignCouponService;
    private final CampaignSubscriptionService campaignSubscriptionService;
    private final CampaignSummaryService campaignSummaryService;
    private final CampaignWinnersService campaignWinnersService;
    private final CampaignAllocationService campaignAllocationService;
    private final CampaignRestMapper campaignRestMapper;

    public CampaignsResource(
            CampaignManagementService campaignManagementService,
            CampaignCouponService campaignCouponService,
            CampaignSubscriptionService campaignSubscriptionService,
            CampaignSummaryService campaignSummaryService,
            CampaignWinnersService campaignWinnersService,
            CampaignAllocationService campaignAllocationService,
            CampaignRestMapper campaignRestMapper) {
        this.campaignManagementService = campaignManagementService;
        this.campaignCouponService = campaignCouponService;
        this.campaignSubscriptionService = campaignSubscriptionService;
        this.campaignSummaryService = campaignSummaryService;
        this.campaignWinnersService = campaignWinnersService;
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

    @GetMapping("/{campaignId}")
    public CampaignResponse get(@PathVariable UUID campaignId) {
        return campaignRestMapper.toResponse(campaignManagementService.getById(campaignId));
    }

    @PatchMapping("/{campaignId}")
    public CampaignResponse patch(@PathVariable UUID campaignId, @Valid @RequestBody PatchCampaignRequest request) {
        return campaignRestMapper.toResponse(campaignManagementService.patchCampaign(campaignId, request));
    }

    @PostMapping("/{campaignId}/coupons")
    public CampaignResponse addCoupon(
            @PathVariable UUID campaignId, @Valid @RequestBody AddCouponToCampaignRequest request) {
        return campaignRestMapper.toResponse(
                campaignCouponService.addCoupon(
                        campaignId, campaignRestMapper.toCouponDraft(request), request.getPriority()));
    }

    @GetMapping("/{campaignId}/coupons")
    public List<CampaignCouponLinkResponse> listCampaignCoupons(@PathVariable UUID campaignId) {
        return campaignCouponService.listLinkedCoupons(campaignId);
    }

    @DeleteMapping("/{campaignId}/coupons/{couponId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeCampaignCoupon(@PathVariable UUID campaignId, @PathVariable UUID couponId) {
        campaignCouponService.removeCouponFromCampaign(campaignId, couponId);
    }

    @PostMapping("/{campaignId}/subscriptions")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void subscribe(@PathVariable UUID campaignId, @Valid @RequestBody UserIdRequest request) {
        campaignSubscriptionService.subscribe(campaignId, request.getUserId());
    }

    @GetMapping("/{campaignId}/subscriptions/me")
    public MyCampaignSubscriptionResponse mySubscription(
            @PathVariable UUID campaignId, @RequestParam("userId") UUID userId) {
        return campaignSubscriptionService.getMySubscriptionStatus(campaignId, userId);
    }

    @PostMapping("/{campaignId}/allocations")
    public AllocationResponse allocate(@PathVariable UUID campaignId, @Valid @RequestBody UserIdRequest request) {
        return campaignRestMapper.toResponse(
                campaignAllocationService.allocate(campaignId, request.getUserId()));
    }

    @GetMapping("/{campaignId}/summary")
    public CampaignSummaryResponse summary(@PathVariable UUID campaignId) {
        return campaignSummaryService.summary(campaignId);
    }

    @GetMapping("/{campaignId}/winners")
    public CampaignWinnersResponse winners(@PathVariable UUID campaignId) {
        return campaignWinnersService.winners(campaignId);
    }
}
