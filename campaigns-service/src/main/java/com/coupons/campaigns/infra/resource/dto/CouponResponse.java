package com.coupons.campaigns.infra.resource.dto;

import com.coupons.campaigns.domain.CouponStatus;
import com.coupons.campaigns.domain.entity.Coupon;
import java.time.Instant;
import java.util.UUID;

public class CouponResponse {

    private UUID id;
    private String code;
    private String title;
    private Instant expiresAt;
    private CouponStatus status;
    private Instant createdAt;

    public static CouponResponse from(Coupon c) {
        CouponResponse r = new CouponResponse();
        r.id = c.getId();
        r.code = c.getCode();
        r.title = c.getTitle();
        r.expiresAt = c.getExpiresAt();
        r.status = c.getStatus();
        r.createdAt = c.getCreatedAt();
        return r;
    }

    public UUID getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public String getTitle() {
        return title;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public CouponStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
