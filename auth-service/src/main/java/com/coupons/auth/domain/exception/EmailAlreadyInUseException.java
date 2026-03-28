package com.coupons.auth.domain.exception;

public class EmailAlreadyInUseException extends RuntimeException {

    public EmailAlreadyInUseException(String email) {
        super("Email já registado: " + email);
    }
}
