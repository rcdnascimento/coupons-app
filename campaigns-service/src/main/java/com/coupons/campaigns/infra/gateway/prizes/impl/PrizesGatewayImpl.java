package com.coupons.campaigns.infra.gateway.prizes.impl;

import com.coupons.campaigns.infra.gateway.prizes.PrizesGateway;
import com.coupons.campaigns.infra.gateway.prizes.dto.PrizeDeliveryResponse;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class PrizesGatewayImpl implements PrizesGateway {

    private final RestTemplate restTemplate;
    private final String prizesBaseUrl;

    public PrizesGatewayImpl(
            RestTemplate restTemplate,
            @Value("${coupons.services.prizes-url:http://localhost:8085}") String prizesBaseUrl) {
        this.restTemplate = restTemplate;
        this.prizesBaseUrl = normalizeBaseUrl(prizesBaseUrl);
    }

    @Override
    public boolean isPrizeDelivered(UUID userId, UUID campaignId, UUID couponId) {
        String url =
                prizesBaseUrl
                        + "/v1/prizes/users/"
                        + userId
                        + "?campaignId="
                        + campaignId;

        ResponseEntity<List<PrizeDeliveryResponse>> response =
                restTemplate.exchange(
                        url,
                        HttpMethod.GET,
                        null,
                        new ParameterizedTypeReference<List<PrizeDeliveryResponse>>() {});

        List<PrizeDeliveryResponse> deliveries = response.getBody();
        if (deliveries == null) {
            return false;
        }

        return deliveries.stream()
                .anyMatch(
                        d ->
                                couponId.equals(d.getCouponId())
                                        && d.getStatus() != null
                                        && "DELIVERED".equalsIgnoreCase(d.getStatus()));
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
