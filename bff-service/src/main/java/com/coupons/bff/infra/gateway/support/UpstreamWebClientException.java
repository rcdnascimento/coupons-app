package com.coupons.bff.infra.gateway.support;

import com.coupons.bff.infra.resource.dto.ErrorResponse;

public class UpstreamWebClientException extends RuntimeException {

    private final int rawStatusCode;
    private final ErrorResponse errorResponse;

    public UpstreamWebClientException(int rawStatusCode, ErrorResponse errorResponse) {
        super(errorResponse != null && errorResponse.getError() != null ? errorResponse.getError() : "");
        this.rawStatusCode = rawStatusCode;
        this.errorResponse = errorResponse;
    }

    public int getRawStatusCode() {
        return rawStatusCode;
    }

    public ErrorResponse getErrorResponse() {
        return errorResponse;
    }
}
