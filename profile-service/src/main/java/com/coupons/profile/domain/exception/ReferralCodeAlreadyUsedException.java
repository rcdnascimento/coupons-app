package com.coupons.profile.domain.exception;

public class ReferralCodeAlreadyUsedException extends RuntimeException {

    public ReferralCodeAlreadyUsedException(String message) {
        super(message);
    }
}
