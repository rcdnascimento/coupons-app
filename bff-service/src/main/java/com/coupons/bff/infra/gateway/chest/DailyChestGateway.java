package com.coupons.bff.infra.gateway.chest;

import com.coupons.bff.infra.resource.dto.DailyChestTodayResponse;
import java.util.UUID;

public interface DailyChestGateway {

    DailyChestTodayResponse open(UUID userId);

    DailyChestTodayResponse today(UUID userId);
}
