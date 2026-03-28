package com.coupons.profile.domain.exception;

import java.util.UUID;

public class ProfileNotFoundException extends RuntimeException {

    public ProfileNotFoundException(UUID userId) {
        super("Perfil não encontrado: " + userId);
    }
}
