package com.coupons.bff.infra.gateway.support;

import com.coupons.bff.infra.resource.dto.ErrorResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Component
public class GatewayHttpSupport {

    private final ObjectMapper objectMapper;

    public GatewayHttpSupport(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public UpstreamWebClientException upstream(WebClientResponseException ex) {
        ErrorResponse err = readError(ex.getResponseBodyAsString());
        return new UpstreamWebClientException(ex.getRawStatusCode(), err);
    }

    public UpstreamWebClientException unavailable(String serviceLabel) {
        return new UpstreamWebClientException(502, new ErrorResponse(serviceLabel + " indisponível"));
    }

    private ErrorResponse readError(String raw) {
        if (raw == null || raw.isBlank()) {
            return new ErrorResponse("Erro desconhecido");
        }
        try {
            return objectMapper.readValue(raw, ErrorResponse.class);
        } catch (JsonProcessingException e) {
            String msg = raw.length() > 400 ? raw.substring(0, 400) : raw;
            return new ErrorResponse(msg);
        }
    }
}
