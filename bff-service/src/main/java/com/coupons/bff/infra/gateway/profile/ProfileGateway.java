package com.coupons.bff.infra.gateway.profile;

import com.coupons.bff.infra.resource.dto.ProfileResponse;

public interface ProfileGateway {

    ProfileResponse getByUserId(String userId);
}
