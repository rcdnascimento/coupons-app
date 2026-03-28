package com.coupons.bff.infra.gateway.auth;

import com.coupons.bff.infra.resource.dto.AuthTokenResponse;
import com.coupons.bff.infra.resource.dto.LoginRequest;
import com.coupons.bff.infra.resource.dto.RegisterRequest;

public interface AuthGateway {

    AuthTokenResponse register(RegisterRequest request);

    AuthTokenResponse login(LoginRequest request);
}
