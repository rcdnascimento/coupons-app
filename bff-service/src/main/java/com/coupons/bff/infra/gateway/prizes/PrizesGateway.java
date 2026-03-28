package com.coupons.bff.infra.gateway.prizes;

import com.coupons.bff.infra.resource.dto.PrizeDeliveryResponse;
import java.util.List;

public interface PrizesGateway {

    List<PrizeDeliveryResponse> prizesByUser(String userId, String campaignId);
}
