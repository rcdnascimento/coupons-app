package com.coupons.bff.resource;

import com.coupons.bff.infra.gateway.campaigns.CampaignsGateway;
import com.coupons.bff.infra.gateway.profile.ProfileGateway;
import com.coupons.bff.infra.resource.dto.AddCouponToCampaignRequest;
import com.coupons.bff.infra.resource.dto.CampaignCouponLinkResponse;
import com.coupons.bff.infra.resource.dto.CampaignResponse;
import com.coupons.bff.infra.resource.dto.CampaignSummaryResponse;
import com.coupons.bff.infra.resource.dto.CampaignWinnerEntry;
import com.coupons.bff.infra.resource.dto.CampaignWinnersResponse;
import com.coupons.bff.infra.resource.dto.MyCampaignSubscriptionResponse;
import com.coupons.bff.infra.resource.dto.CreateCampaignRequest;
import com.coupons.bff.infra.resource.dto.PatchCampaignRequest;
import com.coupons.bff.infra.resource.dto.ProfileResponse;
import com.coupons.bff.infra.resource.dto.UserIdRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/campaigns")
public class CampaignsProxyResource {

    private final CampaignsGateway campaignsGateway;
    private final ProfileGateway profileGateway;

    public CampaignsProxyResource(CampaignsGateway campaignsGateway, ProfileGateway profileGateway) {
        this.campaignsGateway = campaignsGateway;
        this.profileGateway = profileGateway;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CampaignResponse> create(@Valid @RequestBody CreateCampaignRequest body) {
        return ResponseEntity.status(HttpStatus.CREATED).body(campaignsGateway.createCampaign(body));
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<CampaignResponse>> list() {
        return ResponseEntity.ok(campaignsGateway.listCampaigns());
    }

    @GetMapping(value = "/{campaignId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CampaignResponse> getOne(@PathVariable String campaignId) {
        return ResponseEntity.ok(campaignsGateway.getCampaign(campaignId));
    }

    @PatchMapping(
            value = "/{campaignId}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CampaignResponse> patch(
            @PathVariable String campaignId, @Valid @RequestBody PatchCampaignRequest body) {
        return ResponseEntity.ok(campaignsGateway.patchCampaign(campaignId, body));
    }

    @PostMapping(value = "/{campaignId}/coupons", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CampaignResponse> addCoupon(
            @PathVariable String campaignId, @Valid @RequestBody AddCouponToCampaignRequest body) {
        return ResponseEntity.ok(campaignsGateway.addCoupon(campaignId, body));
    }

    @GetMapping(value = "/{campaignId}/coupons", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<CampaignCouponLinkResponse>> listCampaignCoupons(@PathVariable String campaignId) {
        return ResponseEntity.ok(campaignsGateway.listCampaignCoupons(campaignId));
    }

    @DeleteMapping(value = "/{campaignId}/coupons/{couponId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeCampaignCoupon(@PathVariable String campaignId, @PathVariable String couponId) {
        campaignsGateway.removeCampaignCoupon(campaignId, couponId);
    }

    @PostMapping(value = "/{campaignId}/subscriptions", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void subscribe(@PathVariable String campaignId, @Valid @RequestBody UserIdRequest body) {
        campaignsGateway.subscribe(campaignId, body);
    }

    @GetMapping(
            value = "/{campaignId}/subscriptions/me",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<MyCampaignSubscriptionResponse> mySubscription(
            @PathVariable String campaignId, @RequestParam("userId") String userId) {
        return ResponseEntity.ok(campaignsGateway.mySubscription(campaignId, userId));
    }

    @GetMapping(value = "/{campaignId}/summary", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CampaignSummaryResponse> summary(@PathVariable String campaignId) {
        return ResponseEntity.ok(campaignsGateway.summary(campaignId));
    }

    @GetMapping(value = "/{campaignId}/winners", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CampaignWinnersResponse> winners(@PathVariable String campaignId) {
        CampaignWinnersResponse body = campaignsGateway.winners(campaignId);
        if (body.getEntries() == null || body.getEntries().isEmpty()) {
            return ResponseEntity.ok(body);
        }
        Map<String, String> nameByUserId = new HashMap<>();
        for (CampaignWinnerEntry e : body.getEntries()) {
            if (e.getUserId() == null) {
                continue;
            }
            String uid = e.getUserId().toString();
            if (!nameByUserId.containsKey(uid)) {
                String name = null;
                try {
                    ProfileResponse p = profileGateway.getByUserId(uid);
                    if (p != null && p.getDisplayName() != null && !p.getDisplayName().isBlank()) {
                        name = p.getDisplayName().trim();
                    }
                } catch (RuntimeException ignored) {
                    // perfil indisponível
                }
                nameByUserId.put(uid, name);
            }
            e.setWinnerDisplayName(nameByUserId.get(uid));
        }
        return ResponseEntity.ok(body);
    }
}
