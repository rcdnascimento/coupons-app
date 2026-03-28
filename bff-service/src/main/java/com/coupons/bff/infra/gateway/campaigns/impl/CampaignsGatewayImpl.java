package com.coupons.bff.infra.gateway.campaigns.impl;

import com.coupons.bff.infra.gateway.campaigns.CampaignsGateway;
import com.coupons.bff.infra.gateway.support.GatewayHttpSupport;
import com.coupons.bff.infra.resource.dto.AddCouponToCampaignRequest;
import com.coupons.bff.infra.resource.dto.CampaignResponse;
import com.coupons.bff.infra.resource.dto.CreateCampaignRequest;
import com.coupons.bff.infra.resource.dto.UserIdRequest;
import java.util.List;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Component
public class CampaignsGatewayImpl implements CampaignsGateway {

    private final WebClient campaignsWebClient;
    private final GatewayHttpSupport gatewayHttpSupport;

    public CampaignsGatewayImpl(
            @Qualifier("campaignsWebClient") WebClient campaignsWebClient,
            GatewayHttpSupport gatewayHttpSupport) {
        this.campaignsWebClient = campaignsWebClient;
        this.gatewayHttpSupport = gatewayHttpSupport;
    }

    @Override
    public CampaignResponse createCampaign(CreateCampaignRequest request) {
        try {
            return campaignsWebClient
                    .post()
                    .uri("/v1/campaigns")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(CampaignResponse.class)
                    .block();
        } catch (WebClientResponseException ex) {
            throw gatewayHttpSupport.upstream(ex);
        } catch (WebClientException ex) {
            throw gatewayHttpSupport.unavailable("campaigns-service");
        }
    }

    @Override
    public List<CampaignResponse> listCampaigns() {
        try {
            return campaignsWebClient
                    .get()
                    .uri("/v1/campaigns")
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<CampaignResponse>>() {})
                    .block();
        } catch (WebClientResponseException ex) {
            throw gatewayHttpSupport.upstream(ex);
        } catch (WebClientException ex) {
            throw gatewayHttpSupport.unavailable("campaigns-service");
        }
    }

    @Override
    public CampaignResponse addCoupon(String campaignId, AddCouponToCampaignRequest request) {
        try {
            return campaignsWebClient
                    .post()
                    .uri("/v1/campaigns/{campaignId}/coupons", campaignId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(CampaignResponse.class)
                    .block();
        } catch (WebClientResponseException ex) {
            throw gatewayHttpSupport.upstream(ex);
        } catch (WebClientException ex) {
            throw gatewayHttpSupport.unavailable("campaigns-service");
        }
    }

    @Override
    public void subscribe(String campaignId, UserIdRequest request) {
        try {
            campaignsWebClient
                    .post()
                    .uri("/v1/campaigns/{campaignId}/subscriptions", campaignId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
                    .retrieve()
                    .toBodilessEntity()
                    .block();
        } catch (WebClientResponseException ex) {
            throw gatewayHttpSupport.upstream(ex);
        } catch (WebClientException ex) {
            throw gatewayHttpSupport.unavailable("campaigns-service");
        }
    }
}
