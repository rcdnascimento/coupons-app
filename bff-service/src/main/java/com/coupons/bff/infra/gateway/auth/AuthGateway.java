package com.coupons.bff.infra.gateway.auth;

import com.coupons.bff.infra.resource.dto.AdminUserSearchResponse;
import com.coupons.bff.infra.resource.dto.AuthTokenResponse;
import com.coupons.bff.infra.resource.dto.LoginRequest;
import com.coupons.bff.infra.resource.dto.RegisterRequest;
import java.util.List;

public interface AuthGateway {

    AuthTokenResponse register(RegisterRequest request);

    AuthTokenResponse login(LoginRequest request);

    List<AdminUserSearchResponse> searchUsers(String q);
}
