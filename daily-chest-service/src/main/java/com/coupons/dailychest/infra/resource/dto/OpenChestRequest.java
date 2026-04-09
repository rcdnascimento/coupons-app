package com.coupons.dailychest.infra.resource.dto;

import java.util.UUID;
import javax.validation.constraints.NotNull;

public class OpenChestRequest {

    @NotNull
    private UUID userId;

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }
}
