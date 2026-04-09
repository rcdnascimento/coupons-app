package com.coupons.bff.infra.gateway.campaigns.impl;

import com.coupons.bff.infra.gateway.campaigns.CampaignsGateway;
import com.coupons.bff.infra.gateway.support.GatewayHttpSupport;
import com.coupons.bff.infra.resource.dto.AddCouponToCampaignRequest;
import com.coupons.bff.infra.resource.dto.CampaignCouponLinkResponse;
import com.coupons.bff.infra.resource.dto.CampaignResponse;
import com.coupons.bff.infra.resource.dto.CampaignSummaryResponse;
import com.coupons.bff.infra.resource.dto.CampaignWinnersResponse;
import com.coupons.bff.infra.resource.dto.CompanyResponse;
import com.coupons.bff.infra.resource.dto.MyCampaignSubscriptionResponse;
import com.coupons.bff.infra.resource.dto.CreateCompanyRequest;
import com.coupons.bff.infra.resource.dto.CreateCampaignRequest;
import com.coupons.bff.infra.resource.dto.PatchCampaignRequest;
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
    public CompanyResponse createCompany(CreateCompanyRequest request) {
        try {
            return campaignsWebClient
                    .post()
                    .uri("/v1/companies")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(CompanyResponse.class)
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
    public List<CompanyResponse> listCompanies() {
        try {
            return campaignsWebClient
                    .get()
                    .uri("/v1/companies")
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<CompanyResponse>>() {})
                    .block();
        } catch (WebClientResponseException ex) {
            throw gatewayHttpSupport.upstream(ex);
        } catch (WebClientException ex) {
            throw gatewayHttpSupport.unavailable("campaigns-service");
        }
    }

    @Override
    public CampaignResponse getCampaign(String campaignId) {
        try {
            return campaignsWebClient
                    .get()
                    .uri("/v1/campaigns/{campaignId}", campaignId)
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
    public CampaignResponse patchCampaign(String campaignId, PatchCampaignRequest request) {
        try {
            return campaignsWebClient
                    .patch()
                    .uri("/v1/campaigns/{campaignId}", campaignId)
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
    public List<CampaignCouponLinkResponse> listCampaignCoupons(String campaignId) {
        try {
            return campaignsWebClient
                    .get()
                    .uri("/v1/campaigns/{campaignId}/coupons", campaignId)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<CampaignCouponLinkResponse>>() {})
                    .block();
        } catch (WebClientResponseException ex) {
            throw gatewayHttpSupport.upstream(ex);
        } catch (WebClientException ex) {
            throw gatewayHttpSupport.unavailable("campaigns-service");
        }
    }

    @Override
    public void removeCampaignCoupon(String campaignId, String couponId) {
        try {
            campaignsWebClient
                    .delete()
                    .uri("/v1/campaigns/{campaignId}/coupons/{couponId}", campaignId, couponId)
                    .retrieve()
                    .toBodilessEntity()
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

    @Override
    public MyCampaignSubscriptionResponse mySubscription(String campaignId, String userId) {
        try {
            return campaignsWebClient
                    .get()
                    .uri(uriBuilder ->
                            uriBuilder
                                    .path("/v1/campaigns/{campaignId}/subscriptions/me")
                                    .queryParam("userId", userId)
                                    .build(campaignId))
                    .retrieve()
                    .bodyToMono(MyCampaignSubscriptionResponse.class)
                    .block();
        } catch (WebClientResponseException ex) {
            throw gatewayHttpSupport.upstream(ex);
        } catch (WebClientException ex) {
            throw gatewayHttpSupport.unavailable("campaigns-service");
        }
    }

    @Override
    public CampaignSummaryResponse summary(String campaignId) {
        try {
            return campaignsWebClient
                    .get()
                    .uri("/v1/campaigns/{campaignId}/summary", campaignId)
                    .retrieve()
                    .bodyToMono(CampaignSummaryResponse.class)
                    .block();
        } catch (WebClientResponseException ex) {
            throw gatewayHttpSupport.upstream(ex);
        } catch (WebClientException ex) {
            throw gatewayHttpSupport.unavailable("campaigns-service");
        }
    }

    @Override
    public CampaignWinnersResponse winners(String campaignId) {
        try {
            return campaignsWebClient
                    .get()
                    .uri("/v1/campaigns/{campaignId}/winners", campaignId)
                    .retrieve()
                    .bodyToMono(CampaignWinnersResponse.class)
                    .block();
        } catch (WebClientResponseException ex) {
            throw gatewayHttpSupport.upstream(ex);
        } catch (WebClientException ex) {
            throw gatewayHttpSupport.unavailable("campaigns-service");
        }
    }
}
