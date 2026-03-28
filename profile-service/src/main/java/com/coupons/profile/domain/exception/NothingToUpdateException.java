package com.coupons.profile.domain.exception;

public class NothingToUpdateException extends RuntimeException {

    public NothingToUpdateException() {
        super("Indique pelo menos displayName ou timezone para atualizar");
    }
}
