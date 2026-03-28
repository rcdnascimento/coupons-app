package com.coupons.auth.domain.exception;

public class ProfileCreationFailedException extends RuntimeException {
    public ProfileCreationFailedException(String message) {
        super(message);
    }

    public ProfileCreationFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}

