package com.coupons.dailychest.infra.gateway.profile;

import java.util.UUID;

public interface ProfileGateway {

    String resolveTimezoneByUserId(UUID userId);
}
