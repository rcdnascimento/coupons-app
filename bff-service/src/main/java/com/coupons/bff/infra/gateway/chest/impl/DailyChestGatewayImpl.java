package com.coupons.bff.infra.gateway.chest.impl;

import com.coupons.bff.infra.gateway.chest.DailyChestGateway;
import com.coupons.bff.infra.gateway.support.GatewayHttpSupport;
import com.coupons.bff.infra.resource.dto.DailyChestTodayResponse;
import com.coupons.bff.infra.resource.dto.UserIdRequest;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Component
public class DailyChestGatewayImpl implements DailyChestGateway {

    private final WebClient dailyChestWebClient;
    private final GatewayHttpSupport gatewayHttpSupport;

    public DailyChestGatewayImpl(
            @Qualifier("dailyChestWebClient") WebClient dailyChestWebClient,
            GatewayHttpSupport gatewayHttpSupport) {
        this.dailyChestWebClient = dailyChestWebClient;
        this.gatewayHttpSupport = gatewayHttpSupport;
    }

    @Override
    public DailyChestTodayResponse open(UUID userId) {
        UserIdRequest request = new UserIdRequest();
        request.setUserId(userId);
        try {
            return dailyChestWebClient
                    .post()
                    .uri("/v1/daily-chest/open")
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(DailyChestTodayResponse.class)
                    .block();
        } catch (WebClientResponseException ex) {
            throw gatewayHttpSupport.upstream(ex);
        } catch (WebClientException ex) {
            throw gatewayHttpSupport.unavailable("daily-chest-service");
        }
    }

    @Override
    public DailyChestTodayResponse today(UUID userId) {
        try {
            return dailyChestWebClient
                    .get()
                    .uri(uriBuilder -> uriBuilder.path("/v1/daily-chest/today").queryParam("userId", userId).build())
                    .retrieve()
                    .bodyToMono(DailyChestTodayResponse.class)
                    .block();
        } catch (WebClientResponseException ex) {
            throw gatewayHttpSupport.upstream(ex);
        } catch (WebClientException ex) {
            throw gatewayHttpSupport.unavailable("daily-chest-service");
        }
    }
}
