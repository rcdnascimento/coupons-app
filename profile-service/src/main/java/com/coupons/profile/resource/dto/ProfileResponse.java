package com.coupons.profile.infra.resource.dto;

import com.coupons.profile.domain.entity.Profile;
import java.time.Instant;
import java.util.UUID;

public class ProfileResponse {

    private final UUID userId;
    private final String displayName;
    private final String referralCode;
    private final String timezone;
    private final Instant createdAt;

    public ProfileResponse(UUID userId, String displayName, String referralCode, String timezone, Instant createdAt) {
        this.userId = userId;
        this.displayName = displayName;
        this.referralCode = referralCode;
        this.timezone = timezone;
        this.createdAt = createdAt;
    }

    public static ProfileResponse from(Profile p) {
        return new ProfileResponse(
                p.getUserId(), p.getDisplayName(), p.getReferralCode(), p.getTimezone(), p.getCreatedAt());
    }

    public UUID getUserId() {
        return userId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getReferralCode() {
        return referralCode;
    }

    public String getTimezone() {
        return timezone;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
