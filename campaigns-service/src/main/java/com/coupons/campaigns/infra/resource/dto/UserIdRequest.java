package com.coupons.campaigns.infra.resource.dto;

import java.util.UUID;
import javax.validation.constraints.NotNull;

public class UserIdRequest {

    @NotNull
    private UUID userId;

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }
}
