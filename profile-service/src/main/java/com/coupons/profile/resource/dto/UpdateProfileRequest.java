package com.coupons.profile.infra.resource.dto;

import javax.validation.constraints.Size;

public class UpdateProfileRequest {

    @Size(max = 255)
    private String displayName;

    @Size(max = 64)
    private String timezone;

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
