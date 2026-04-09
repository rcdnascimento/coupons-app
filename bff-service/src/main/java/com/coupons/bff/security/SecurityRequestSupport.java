package com.coupons.bff.security;

import java.util.UUID;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityRequestSupport {

    private SecurityRequestSupport() {}

    public static UUID currentUserIdOrNull() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return null;
        }
        Object p = auth.getPrincipal();
        if (p instanceof BffUserPrincipal) {
            return ((BffUserPrincipal) p).getUserId();
        }
        return null;
    }

    public static UUID requireUserId() {
        UUID id = currentUserIdOrNull();
        if (id == null) {
            throw new AccessDeniedException("Sessão inválida");
        }
        return id;
    }
}
