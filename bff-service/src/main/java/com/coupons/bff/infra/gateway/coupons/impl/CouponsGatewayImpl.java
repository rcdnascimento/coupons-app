package com.coupons.bff.infra.gateway.coupons.impl;

import com.coupons.bff.infra.gateway.coupons.CouponsGateway;
import com.coupons.bff.infra.gateway.support.GatewayHttpSupport;
import com.coupons.bff.infra.resource.dto.CouponResponse;
import com.coupons.bff.infra.resource.dto.CreateCouponRequest;
import com.coupons.bff.infra.resource.dto.UpdateCouponRequest;
import java.util.List;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Component
public class CouponsGatewayImpl implements CouponsGateway {

    private final WebClient campaignsWebClient;
    private final GatewayHttpSupport gatewayHttpSupport;

    public CouponsGatewayImpl(
            @Qualifier("campaignsWebClient") WebClient campaignsWebClient,
            GatewayHttpSupport gatewayHttpSupport) {
        this.campaignsWebClient = campaignsWebClient;
        this.gatewayHttpSupport = gatewayHttpSupport;
    }

    @Override
    public List<CouponResponse> listCoupons() {
        try {
            return campaignsWebClient
                    .get()
                    .uri("/v1/coupons")
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<CouponResponse>>() {})
                    .block();
        } catch (WebClientResponseException ex) {
            throw gatewayHttpSupport.upstream(ex);
        } catch (WebClientException ex) {
            throw gatewayHttpSupport.unavailable("campaigns-service");
        }
    }

    @Override
    public List<CouponResponse> searchCoupons(String q, String status) {
        try {
            return campaignsWebClient
                    .get()
                    .uri(uriBuilder -> {
                        var b = uriBuilder.path("/v1/coupons/search").queryParam("q", q == null ? "" : q);
                        if (status != null && !status.isBlank()) {
                            b.queryParam("status", status.trim());
                        }
                        return b.build();
                    })
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<CouponResponse>>() {})
                    .block();
        } catch (WebClientResponseException ex) {
            throw gatewayHttpSupport.upstream(ex);
        } catch (WebClientException ex) {
            throw gatewayHttpSupport.unavailable("campaigns-service");
        }
    }

    @Override
    public CouponResponse getCoupon(String couponId) {
        try {
            return campaignsWebClient
                    .get()
                    .uri("/v1/coupons/{couponId}", couponId)
                    .retrieve()
                    .bodyToMono(CouponResponse.class)
                    .block();
        } catch (WebClientResponseException ex) {
            throw gatewayHttpSupport.upstream(ex);
        } catch (WebClientException ex) {
            throw gatewayHttpSupport.unavailable("campaigns-service");
        }
    }

    @Override
    public CouponResponse createCoupon(CreateCouponRequest request) {
        try {
            return campaignsWebClient
                    .post()
                    .uri("/v1/coupons")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(CouponResponse.class)
                    .block();
        } catch (WebClientResponseException ex) {
            throw gatewayHttpSupport.upstream(ex);
        } catch (WebClientException ex) {
            throw gatewayHttpSupport.unavailable("campaigns-service");
        }
    }

    @Override
    public CouponResponse patchCoupon(String couponId, UpdateCouponRequest request) {
        try {
            return campaignsWebClient
                    .patch()
                    .uri("/v1/coupons/{couponId}", couponId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(CouponResponse.class)
                    .block();
        } catch (WebClientResponseException ex) {
            throw gatewayHttpSupport.upstream(ex);
        } catch (WebClientException ex) {
            throw gatewayHttpSupport.unavailable("campaigns-service");
        }
    }

    @Override
    public void deleteCoupon(String couponId) {
        try {
            campaignsWebClient
                    .delete()
                    .uri("/v1/coupons/{couponId}", couponId)
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
