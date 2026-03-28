package com.coupons.campaigns.infra.gateway.prizes;

import java.util.UUID;

public interface PrizesGateway {

    boolean isPrizeDelivered(UUID userId, UUID campaignId, UUID couponId);
}
