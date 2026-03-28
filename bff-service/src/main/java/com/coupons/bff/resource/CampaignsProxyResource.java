package com.coupons.bff.resource;

import com.coupons.bff.infra.gateway.campaigns.CampaignsGateway;
import com.coupons.bff.infra.resource.dto.AddCouponToCampaignRequest;
import com.coupons.bff.infra.resource.dto.CampaignResponse;
import com.coupons.bff.infra.resource.dto.CreateCampaignRequest;
import com.coupons.bff.infra.resource.dto.UserIdRequest;
import java.util.List;
import javax.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/campaigns")
public class CampaignsProxyResource {

    private final CampaignsGateway campaignsGateway;

    public CampaignsProxyResource(CampaignsGateway campaignsGateway) {
        this.campaignsGateway = campaignsGateway;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CampaignResponse> create(@Valid @RequestBody CreateCampaignRequest body) {
        return ResponseEntity.status(HttpStatus.CREATED).body(campaignsGateway.createCampaign(body));
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<CampaignResponse>> list() {
        return ResponseEntity.ok(campaignsGateway.listCampaigns());
    }

    @PostMapping(value = "/{campaignId}/coupons", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CampaignResponse> addCoupon(
            @PathVariable String campaignId, @Valid @RequestBody AddCouponToCampaignRequest body) {
        return ResponseEntity.ok(campaignsGateway.addCoupon(campaignId, body));
    }

    @PostMapping(value = "/{campaignId}/subscriptions", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void subscribe(@PathVariable String campaignId, @Valid @RequestBody UserIdRequest body) {
        campaignsGateway.subscribe(campaignId, body);
    }
}
