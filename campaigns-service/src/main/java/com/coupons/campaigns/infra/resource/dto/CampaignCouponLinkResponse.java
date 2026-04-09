package com.coupons.campaigns.infra.resource.dto;

import java.util.UUID;

public class CampaignCouponLinkResponse {

    private UUID couponId;
    private String code;
    private String title;
    private Integer priority;
    private boolean allocated;

    public UUID getCouponId() {
        return couponId;
    }

    public void setCouponId(UUID couponId) {
        this.couponId = couponId;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public boolean isAllocated() {
        return allocated;
    }

    public void setAllocated(boolean allocated) {
        this.allocated = allocated;
    }
}
