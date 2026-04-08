package com.coupons.auth.infra.gateway.profile.impl;

import com.coupons.auth.domain.exception.InvalidReferralCodeException;
import com.coupons.auth.domain.exception.ProfileCreationFailedException;
import com.coupons.auth.infra.gateway.profile.ProfileGateway;
import com.coupons.auth.infra.gateway.profile.dto.ProfileCreateRequest;
import com.coupons.auth.infra.gateway.profile.dto.ProfileRemoteResponse;
import java.net.URI;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Component
public class ProfileGatewayImpl implements ProfileGateway {

    private final RestTemplate restTemplate;
    private final String profileBaseUrl;

    public ProfileGatewayImpl(
            RestTemplate restTemplate, @Value("${coupons.services.profile-url}") String profileBaseUrl) {
        this.restTemplate = restTemplate;
        this.profileBaseUrl = normalizeBaseUrl(profileBaseUrl);
    }

    @Override
    public void createProfile(UUID userId, String displayName, String timezone, String referralCode) {
        ProfileCreateRequest request = new ProfileCreateRequest(userId, displayName, timezone, referralCode);
        URI uri = URI.create(profileBaseUrl + "/v1/profiles");
        try {
            ResponseEntity<ProfileRemoteResponse> resp =
                    restTemplate.postForEntity(uri, request, ProfileRemoteResponse.class);
            if (!resp.getStatusCode().is2xxSuccessful()) {
                throw new ProfileCreationFailedException(
                        "Profile service retornou status nao-sucedido: " + resp.getStatusCode());
            }
        } catch (HttpClientErrorException ex) {
            if (ex.getStatusCode() == HttpStatus.CONFLICT) {
                return;
            }
            if (ex.getStatusCode() == HttpStatus.BAD_REQUEST) {
                throw new InvalidReferralCodeException(
                        "Código de indicação inválido ou já utilizado.", ex);
            }
            throw new ProfileCreationFailedException(
                    "Falha ao criar profile via profile-service. Status=" + ex.getStatusCode(), ex);
        } catch (RestClientException ex) {
            throw new ProfileCreationFailedException(
                    "Falha de comunicacao com profile-service ao criar profile", ex);
        }
    }

    private static String normalizeBaseUrl(String baseUrl) {
        if (baseUrl == null) {
            return "";
        }
        String t = baseUrl.trim();
        while (t.endsWith("/")) {
            t = t.substring(0, t.length() - 1);
        }
        return t;
    }
}
