package com.coupons.profile.infra.resource.dto;

import java.util.UUID;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class CreateProfileRequest {

    @NotNull
    private UUID userId;

    @NotBlank
    @Size(max = 255)
    private String displayName;

    @Size(max = 64)
    private String timezone;

    /** Opcional: código de indicação de outro utilizador (validação síncrona). */
    @Size(max = 64)
    private String referralCode;

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
