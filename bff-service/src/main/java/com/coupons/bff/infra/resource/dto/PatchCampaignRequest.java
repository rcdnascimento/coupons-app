package com.coupons.bff.infra.resource.dto;

import java.time.Instant;
import java.util.UUID;
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

    private UUID companyId;

    private Boolean clearCompany;

    @Size(max = 1024)
    private String imageUrl;

    private Boolean clearImageUrl;

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

    public UUID getCompanyId() {
        return companyId;
    }

    public void setCompanyId(UUID companyId) {
        this.companyId = companyId;
    }

    public Boolean getClearCompany() {
        return clearCompany;
    }

    public void setClearCompany(Boolean clearCompany) {
        this.clearCompany = clearCompany;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Boolean getClearImageUrl() {
        return clearImageUrl;
    }

    public void setClearImageUrl(Boolean clearImageUrl) {
        this.clearImageUrl = clearImageUrl;
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
