package com.coupons.bff.infra.resource.dto;

import java.time.Instant;
import javax.validation.constraints.Min;
import javax.validation.constraints.Size;

public class PatchCampaignRequest {

    @Size(max = 512)
    private String title;

    @Size(max = 2000)
    private String description;

    private Instant subscriptionsStartAt;

    private Instant subscriptionsEndAt;

    private Instant distributionAt;

    private Instant visibleUntil;

    private Boolean clearVisibleUntil;

    @Min(0)
    private Integer pointsCost;

    private CampaignStatus status;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Instant getSubscriptionsStartAt() {
        return subscriptionsStartAt;
    }

    public void setSubscriptionsStartAt(Instant subscriptionsStartAt) {
        this.subscriptionsStartAt = subscriptionsStartAt;
    }

    public Instant getSubscriptionsEndAt() {
        return subscriptionsEndAt;
    }

    public void setSubscriptionsEndAt(Instant subscriptionsEndAt) {
        this.subscriptionsEndAt = subscriptionsEndAt;
    }

    public Instant getDistributionAt() {
        return distributionAt;
    }

    public void setDistributionAt(Instant distributionAt) {
        this.distributionAt = distributionAt;
    }

    public Instant getVisibleUntil() {
        return visibleUntil;
    }

    public void setVisibleUntil(Instant visibleUntil) {
        this.visibleUntil = visibleUntil;
    }

    public Boolean getClearVisibleUntil() {
        return clearVisibleUntil;
    }

    public void setClearVisibleUntil(Boolean clearVisibleUntil) {
        this.clearVisibleUntil = clearVisibleUntil;
    }

    public Integer getPointsCost() {
        return pointsCost;
    }

    public void setPointsCost(Integer pointsCost) {
        this.pointsCost = pointsCost;
    }

    public CampaignStatus getStatus() {
        return status;
    }

    public void setStatus(CampaignStatus status) {
        this.status = status;
    }
}
