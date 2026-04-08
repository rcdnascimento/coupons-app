package com.coupons.auth.infra.gateway.profile.dto;

import java.util.UUID;

public class ProfileCreateRequest {
    private UUID userId;
    private String displayName;
    private String timezone;
    private String referralCode;

    public ProfileCreateRequest() {}

    public ProfileCreateRequest(UUID userId, String displayName, String timezone, String referralCode) {
        this.userId = userId;
        this.displayName = displayName;
        this.timezone = timezone;
        this.referralCode = referralCode;
    }

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

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    public String getReferralCode() {
        return referralCode;
    }

    public void setReferralCode(String referralCode) {
        this.referralCode = referralCode;
    }
}
