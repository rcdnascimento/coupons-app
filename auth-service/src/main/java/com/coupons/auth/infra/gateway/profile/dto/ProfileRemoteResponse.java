package com.coupons.auth.infra.gateway.profile.dto;

import java.time.Instant;
import java.util.UUID;

/**
 * Corpo JSON devolvido pelo profile-service ao criar perfil (espelha {@code ProfileResponse} remoto).
 */
public class ProfileRemoteResponse {

    private UUID userId;
    private String displayName;
    private String referralCode;
    private String timezone;
    private Instant createdAt;

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getReferralCode() {
        return referralCode;
    }

    public void setReferralCode(String referralCode) {
        this.referralCode = referralCode;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
