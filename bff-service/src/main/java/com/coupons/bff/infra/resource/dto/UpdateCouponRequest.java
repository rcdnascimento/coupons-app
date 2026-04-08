package com.coupons.bff.infra.resource.dto;

import java.time.Instant;
import javax.validation.constraints.Size;

public class UpdateCouponRequest {

    @Size(max = 255)
    private String title;

    private Instant expiresAt;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }
}
