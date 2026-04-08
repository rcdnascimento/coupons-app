package com.coupons.campaigns.infra.resource.dto;

import com.coupons.campaigns.domain.CampaignStatus;
import com.coupons.campaigns.domain.entity.Campaign;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import java.time.Instant;
import java.util.UUID;

@JsonAutoDetect(
        fieldVisibility = JsonAutoDetect.Visibility.ANY,
        getterVisibility = JsonAutoDetect.Visibility.PUBLIC_ONLY,
        isGetterVisibility = JsonAutoDetect.Visibility.NONE)
public class CampaignResponse {

    private UUID id;
    private String title;
    private String description;
    private Instant subscriptionsStartAt;
    private Instant subscriptionsEndAt;
    private Instant distributionAt;
    private Instant visibleUntil;
    private CampaignStatus status;
    private int pointsCost;
    private Instant createdAt;
    private Instant updatedAt;

    public static CampaignResponse from(Campaign c) {
        CampaignResponse r = new CampaignResponse();
        r.id = c.getId();
        r.title = c.getTitle();
        r.description = c.getDescription() != null ? c.getDescription() : "";
        r.subscriptionsStartAt = c.getSubscriptionsStartAt();
        r.subscriptionsEndAt = c.getSubscriptionsEndAt();
        r.distributionAt = c.getDistributionAt();
        r.visibleUntil = c.getVisibleUntil();
        r.status = c.getStatus();
        r.pointsCost = c.getPointsCost();
        r.createdAt = c.getCreatedAt();
        r.updatedAt = c.getUpdatedAt();
        return r;
    }

    public UUID getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public Instant getSubscriptionsStartAt() {
        return subscriptionsStartAt;
    }

    public Instant getSubscriptionsEndAt() {
        return subscriptionsEndAt;
    }

    public Instant getDistributionAt() {
        return distributionAt;
    }

    public Instant getVisibleUntil() {
        return visibleUntil;
    }

    public CampaignStatus getStatus() {
        return status;
    }

    public int getPointsCost() {
        return pointsCost;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
