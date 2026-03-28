package com.coupons.profile.domain.exception;

import java.util.UUID;

public class ProfileAlreadyExistsException extends RuntimeException {

    public ProfileAlreadyExistsException(UUID userId) {
        super("Já existe perfil para o utilizador: " + userId);
    }
}
