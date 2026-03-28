package com.coupons.campaigns.infra.gateway.prizes.dto;

import java.util.UUID;

public class PrizeDeliveryResponse {
    private UUID couponId;
    private String status;

    public UUID getCouponId() {
        return couponId;
    }

    public void setCouponId(UUID couponId) {
        this.couponId = couponId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
