package com.coupons.bff.infra.resource.dto;

import java.time.Instant;
import java.util.UUID;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class CreateCampaignRequest {

    @NotBlank
    @Size(max = 512)
    private String title;

    @NotBlank
    @Size(max = 2000)
    private String description;

    @NotNull
    private Instant subscriptionsStartAt;

    @NotNull
    private Instant subscriptionsEndAt;

    @NotNull
    private Instant distributionAt;

    private UUID companyId;

    @Size(max = 1024)
    private String imageUrl;

    private Instant visibleUntil;

    @NotNull
    @Min(0)
    private Integer pointsCost;

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

    public UUID getCompanyId() {
        return companyId;
    }

    public void setCompanyId(UUID companyId) {
        this.companyId = companyId;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Instant getVisibleUntil() {
        return visibleUntil;
    }

    public void setVisibleUntil(Instant visibleUntil) {
        this.visibleUntil = visibleUntil;
    }

    public Integer getPointsCost() {
        return pointsCost;
    }

    public void setPointsCost(Integer pointsCost) {
        this.pointsCost = pointsCost;
    }
}
