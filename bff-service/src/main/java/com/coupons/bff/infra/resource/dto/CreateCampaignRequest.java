package com.coupons.bff.infra.resource.dto;

import java.time.Instant;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class CreateCampaignRequest {

    @NotBlank
    @Size(max = 512)
    private String title;

    @NotNull
    private Instant subscriptionsStartAt;

    @NotNull
    private Instant subscriptionsEndAt;

    @NotNull
    private Instant distributionAt;

    @NotNull
    @Min(0)
    private Integer pointsCost;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
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

    public Integer getPointsCost() {
        return pointsCost;
    }

    public void setPointsCost(Integer pointsCost) {
        this.pointsCost = pointsCost;
    }
}
