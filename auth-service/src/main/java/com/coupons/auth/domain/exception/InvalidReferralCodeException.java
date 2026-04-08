package com.coupons.auth.domain.exception;

public class InvalidReferralCodeException extends RuntimeException {

    public InvalidReferralCodeException(String message) {
        super(message);
    }

    public InvalidReferralCodeException(String message, Throwable cause) {
        super(message, cause);
    }
}
