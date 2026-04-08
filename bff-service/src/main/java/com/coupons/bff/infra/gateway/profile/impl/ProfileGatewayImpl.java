package com.coupons.bff.infra.gateway.profile.impl;

import com.coupons.bff.infra.gateway.profile.ProfileGateway;
import com.coupons.bff.infra.gateway.support.GatewayHttpSupport;
import com.coupons.bff.infra.resource.dto.ProfileResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Component
public class ProfileGatewayImpl implements ProfileGateway {

    private final WebClient profileWebClient;
    private final GatewayHttpSupport gatewayHttpSupport;

    public ProfileGatewayImpl(
            @Qualifier("profileWebClient") WebClient profileWebClient, GatewayHttpSupport gatewayHttpSupport) {
        this.profileWebClient = profileWebClient;
        this.gatewayHttpSupport = gatewayHttpSupport;
    }

    @Override
    public ProfileResponse getByUserId(String userId) {
        try {
            return profileWebClient
                    .get()
                    .uri("/v1/profiles/{userId}", userId)
                    .retrieve()
                    .bodyToMono(ProfileResponse.class)
                    .block();
        } catch (WebClientResponseException ex) {
            throw gatewayHttpSupport.upstream(ex);
        } catch (WebClientException ex) {
            throw gatewayHttpSupport.unavailable("profile-service");
        }
    }
}
