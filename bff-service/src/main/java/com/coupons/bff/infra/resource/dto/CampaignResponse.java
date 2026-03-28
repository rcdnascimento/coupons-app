package com.coupons.bff.infra.resource.dto;

import java.time.Instant;
import java.util.UUID;

public class CampaignResponse {

    private UUID id;
    private String title;
    private Instant subscriptionsStartAt;
    private Instant subscriptionsEndAt;
    private Instant distributionAt;
    private CampaignStatus status;
    private int pointsCost;
    private Instant createdAt;
    private Instant updatedAt;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

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

    public CampaignStatus getStatus() {
        return status;
    }

    public void setStatus(CampaignStatus status) {
        this.status = status;
    }

    public int getPointsCost() {
        return pointsCost;
    }

    public void setPointsCost(int pointsCost) {
        this.pointsCost = pointsCost;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
