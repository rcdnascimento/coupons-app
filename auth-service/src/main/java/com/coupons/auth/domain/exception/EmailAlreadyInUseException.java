package com.coupons.auth.domain.exception;

public class EmailAlreadyInUseException extends RuntimeException {

    public EmailAlreadyInUseException() {
        super("O e-mail informado está em uso.");
    }
}
