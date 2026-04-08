package com.coupons.bff.infra.gateway.auth.impl;

import com.coupons.bff.infra.gateway.auth.AuthGateway;
import com.coupons.bff.infra.gateway.support.GatewayHttpSupport;
import com.coupons.bff.infra.resource.dto.AdminUserSearchResponse;
import com.coupons.bff.infra.resource.dto.AuthTokenResponse;
import com.coupons.bff.infra.resource.dto.LoginRequest;
import com.coupons.bff.infra.resource.dto.RegisterRequest;
import java.util.List;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Component
public class AuthGatewayImpl implements AuthGateway {

    private final WebClient authWebClient;
    private final GatewayHttpSupport gatewayHttpSupport;

    public AuthGatewayImpl(
            @Qualifier("authWebClient") WebClient authWebClient, GatewayHttpSupport gatewayHttpSupport) {
        this.authWebClient = authWebClient;
        this.gatewayHttpSupport = gatewayHttpSupport;
    }

    @Override
    public AuthTokenResponse register(RegisterRequest request) {
        try {
            return authWebClient
                    .post()
                    .uri("/v1/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(AuthTokenResponse.class)
                    .block();
        } catch (WebClientResponseException ex) {
            throw gatewayHttpSupport.upstream(ex);
        } catch (WebClientException ex) {
            throw gatewayHttpSupport.unavailable("auth-service");
        }
    }

    @Override
    public AuthTokenResponse login(LoginRequest request) {
        try {
            return authWebClient
                    .post()
                    .uri("/v1/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(AuthTokenResponse.class)
                    .block();
        } catch (WebClientResponseException ex) {
            throw gatewayHttpSupport.upstream(ex);
        } catch (WebClientException ex) {
            throw gatewayHttpSupport.unavailable("auth-service");
        }
    }

    @Override
    public List<AdminUserSearchResponse> searchUsers(String q) {
        try {
            return authWebClient
                    .get()
                    .uri(uriBuilder -> uriBuilder.path("/v1/admin/users/search").queryParam("q", q).build())
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<AdminUserSearchResponse>>() {})
                    .block();
        } catch (WebClientResponseException ex) {
            throw gatewayHttpSupport.upstream(ex);
        } catch (WebClientException ex) {
            throw gatewayHttpSupport.unavailable("auth-service");
        }
    }
}
