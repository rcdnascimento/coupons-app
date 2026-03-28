package com.coupons.campaigns.infra.resource.dto;

import com.coupons.campaigns.domain.entity.CampaignAllocation;
import java.time.Instant;
import java.util.UUID;

public class AllocationResponse {

    private UUID id;
    private UUID campaignId;
    private UUID userId;
    private UUID couponId;
    private String codeSnapshot;
    private Instant allocatedAt;

    public static AllocationResponse from(CampaignAllocation a) {
        AllocationResponse r = new AllocationResponse();
        r.id = a.getId();
        r.campaignId = a.getCampaignId();
        r.userId = a.getUserId();
        r.couponId = a.getCouponId();
        r.codeSnapshot = a.getCodeSnapshot();
        r.allocatedAt = a.getAllocatedAt();
        return r;
    }

    public UUID getId() {
        return id;
    }

    public UUID getCampaignId() {
        return campaignId;
    }

    public UUID getUserId() {
        return userId;
    }

    public UUID getCouponId() {
        return couponId;
    }

    public String getCodeSnapshot() {
        return codeSnapshot;
    }

    public Instant getAllocatedAt() {
        return allocatedAt;
    }
}
