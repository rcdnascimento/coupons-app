package com.coupons.auth.infra.gateway.profile;

import java.util.UUID;

public interface ProfileGateway {

    void createProfile(UUID userId, String displayName, String timezone, String referralCode);
}
