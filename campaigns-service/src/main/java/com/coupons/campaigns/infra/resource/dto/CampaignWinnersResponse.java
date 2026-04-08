package com.coupons.campaigns.infra.resource.dto;

import java.util.List;
import java.util.UUID;

public class CampaignWinnersResponse {

    private List<WinnerEntry> entries;

    public List<WinnerEntry> getEntries() {
        return entries;
    }

    public void setEntries(List<WinnerEntry> entries) {
        this.entries = entries;
    }

    public static class WinnerEntry {

        private int rank;
        private String couponTitle;
        private Integer priority;
        private UUID userId;

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
    }
}
