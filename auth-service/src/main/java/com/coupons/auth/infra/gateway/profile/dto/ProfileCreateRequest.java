package com.coupons.auth.infra.gateway.profile.dto;

import java.util.UUID;

public class ProfileCreateRequest {
    private UUID userId;
    private String displayName;
    private String timezone;

    public ProfileCreateRequest() {}

    public ProfileCreateRequest(UUID userId, String displayName, String timezone) {
        this.userId = userId;
        this.displayName = displayName;
        this.timezone = timezone;
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
}
