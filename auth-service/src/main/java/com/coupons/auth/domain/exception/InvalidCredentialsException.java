package com.coupons.auth.domain.exception;

public class InvalidCredentialsException extends RuntimeException {

    public InvalidCredentialsException() {
        super("Email ou palavra-passe inválidos");
    }
}
