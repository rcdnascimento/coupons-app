package com.coupons.prizes.infra.resource.dto;

import com.coupons.prizes.domain.entity.PrizeDelivery;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import java.time.Instant;
import java.util.UUID;

@JsonAutoDetect(
        fieldVisibility = JsonAutoDetect.Visibility.ANY,
        getterVisibility = JsonAutoDetect.Visibility.PUBLIC_ONLY,
        isGetterVisibility = JsonAutoDetect.Visibility.NONE)
public class PrizeDeliveryResponse {

    private UUID id;
    private UUID campaignId;
    private UUID userId;
    private UUID couponId;
    private String couponCode;
    private String status;
    private Instant processedAt;

    public static PrizeDeliveryResponse from(PrizeDelivery delivery) {
        PrizeDeliveryResponse response = new PrizeDeliveryResponse();
        response.id = delivery.getId();
        response.campaignId = delivery.getCampaignId();
        response.userId = delivery.getUserId();
        response.couponId = delivery.getCouponId();
        response.couponCode = delivery.getCouponCode();
        response.status = delivery.getStatus();
        response.processedAt = delivery.getProcessedAt();
        return response;
    }

    public UUID getId() {
        return id;
    }

    public UUID getCampaignId() {
        return campaignId;
    }

    public UUID getUserId() {
        return userId;
    }

    public UUID getCouponId() {
        return couponId;
    }

    public String getCouponCode() {
        return couponCode;
    }

    public String getStatus() {
        return status;
    }

    public Instant getProcessedAt() {
        return processedAt;
    }
}

