package com.coupons.campaigns.domain;

public enum CouponStatus {
    IN_INVENTORY,
    ATTACHED_TO_CAMPAIGN,
    /** Atribuído a um participante (existe {@code CampaignAllocation}); entrega pode estar pendente. */
    ASSIGNED,
    DELIVERED,
    VOID
}
