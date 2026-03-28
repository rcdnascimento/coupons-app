package com.coupons.bff.infra.gateway.prizes.impl;

import com.coupons.bff.infra.gateway.prizes.PrizesGateway;
import com.coupons.bff.infra.gateway.support.GatewayHttpSupport;
import com.coupons.bff.infra.resource.dto.PrizeDeliveryResponse;
import java.util.List;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class PrizesGatewayImpl implements PrizesGateway {

    private final WebClient prizesWebClient;
    private final GatewayHttpSupport gatewayHttpSupport;

    public PrizesGatewayImpl(
            @Qualifier("prizesWebClient") WebClient prizesWebClient, GatewayHttpSupport gatewayHttpSupport) {
        this.prizesWebClient = prizesWebClient;
        this.gatewayHttpSupport = gatewayHttpSupport;
    }

    @Override
    public List<PrizeDeliveryResponse> prizesByUser(String userId, String campaignId) {
        UriComponentsBuilder b = UriComponentsBuilder.fromPath("/v1/prizes/users/{userId}");
        if (campaignId != null && !campaignId.isBlank()) {
            b.queryParam("campaignId", campaignId);
        }
        String path = b.buildAndExpand(userId).toUriString();
        try {
            return prizesWebClient
                    .get()
                    .uri(path)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<PrizeDeliveryResponse>>() {})
                    .block();
        } catch (WebClientResponseException ex) {
            throw gatewayHttpSupport.upstream(ex);
        } catch (WebClientException ex) {
            throw gatewayHttpSupport.unavailable("prizes-service");
        }
    }
}
