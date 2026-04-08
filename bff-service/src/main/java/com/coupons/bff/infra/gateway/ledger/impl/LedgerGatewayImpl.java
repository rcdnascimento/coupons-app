package com.coupons.bff.infra.gateway.ledger.impl;

import com.coupons.bff.infra.gateway.ledger.LedgerGateway;
import com.coupons.bff.infra.gateway.support.GatewayHttpSupport;
import com.coupons.bff.infra.resource.dto.BalanceResponse;
import com.coupons.bff.infra.resource.dto.LedgerEntryRequest;
import com.coupons.bff.infra.resource.dto.LedgerEntryResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Component
public class LedgerGatewayImpl implements LedgerGateway {

    private final WebClient ledgerWebClient;
    private final GatewayHttpSupport gatewayHttpSupport;

    public LedgerGatewayImpl(
            @Qualifier("ledgerWebClient") WebClient ledgerWebClient, GatewayHttpSupport gatewayHttpSupport) {
        this.ledgerWebClient = ledgerWebClient;
        this.gatewayHttpSupport = gatewayHttpSupport;
    }

    @Override
    public BalanceResponse getBalance(String userId) {
        try {
            return ledgerWebClient
                    .get()
                    .uri("/v1/ledger/balance/{userId}", userId)
                    .retrieve()
                    .bodyToMono(BalanceResponse.class)
                    .block();
        } catch (WebClientResponseException ex) {
            throw gatewayHttpSupport.upstream(ex);
        } catch (WebClientException ex) {
            throw gatewayHttpSupport.unavailable("ledger-service");
        }
    }

    @Override
    public LedgerEntryResponse credit(LedgerEntryRequest request) {
        try {
            return ledgerWebClient
                    .post()
                    .uri("/v1/ledger/credit")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(LedgerEntryResponse.class)
                    .block();
        } catch (WebClientResponseException ex) {
            throw gatewayHttpSupport.upstream(ex);
        } catch (WebClientException ex) {
            throw gatewayHttpSupport.unavailable("ledger-service");
        }
    }
}
