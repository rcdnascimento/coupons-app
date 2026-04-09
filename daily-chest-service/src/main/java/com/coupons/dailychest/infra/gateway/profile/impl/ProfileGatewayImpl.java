package com.coupons.dailychest.infra.gateway.profile.impl;

import com.coupons.dailychest.infra.gateway.profile.ProfileGateway;
import com.coupons.dailychest.infra.gateway.profile.dto.ProfileResponse;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Component
public class ProfileGatewayImpl implements ProfileGateway {

    private final RestTemplate restTemplate;
    private final String profileBaseUrl;

    public ProfileGatewayImpl(
            RestTemplateBuilder restTemplateBuilder,
            @Value("${coupons.profile.base-url:http://localhost:8082}") String profileBaseUrl,
            @Value("${coupons.ingress.internal-api-key:}") String internalApiKey) {
        RestTemplateBuilder b = restTemplateBuilder;
        if (internalApiKey != null && !internalApiKey.isBlank()) {
            b = b.additionalInterceptors(
                    (request, body, execution) -> {
                        request.getHeaders().set("X-Internal-Api-Key", internalApiKey);
                        return execution.execute(request, body);
                    });
        }
        this.restTemplate = b.build();
        this.profileBaseUrl = profileBaseUrl;
    }

    @Override
    public String resolveTimezoneByUserId(UUID userId) {
        try {
            ResponseEntity<ProfileResponse> response =
                    restTemplate.getForEntity(
                            profileBaseUrl + "/v1/profiles/{userId}", ProfileResponse.class, userId.toString());
            ProfileResponse body = response.getBody();
            if (body == null || body.getTimezone() == null || body.getTimezone().trim().isEmpty()) {
                return "America/Sao_Paulo";
            }
            return body.getTimezone().trim();
        } catch (RestClientException ex) {
            return "America/Sao_Paulo";
        }
    }
}
