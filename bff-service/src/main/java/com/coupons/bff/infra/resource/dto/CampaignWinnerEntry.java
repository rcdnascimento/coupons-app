package com.coupons.bff.infra.resource.dto;

import java.util.UUID;

public class CampaignWinnerEntry {

    private int rank;
    private String couponTitle;
    private Integer priority;
    private UUID userId;
    private String winnerDisplayName;

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public String getCouponTitle() {
        return couponTitle;
    }

    public void setCouponTitle(String couponTitle) {
        this.couponTitle = couponTitle;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getWinnerDisplayName() {
        return winnerDisplayName;
    }

    public void setWinnerDisplayName(String winnerDisplayName) {
        this.winnerDisplayName = winnerDisplayName;
    }
}
