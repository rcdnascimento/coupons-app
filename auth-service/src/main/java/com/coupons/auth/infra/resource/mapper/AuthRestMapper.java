package com.coupons.auth.infra.resource.mapper;

import com.coupons.auth.domain.entity.User;
import com.coupons.auth.infra.resource.dto.AuthResponse;
import com.coupons.auth.infra.resource.dto.RegisterRequest;
import org.springframework.stereotype.Component;

@Component
public class AuthRestMapper {

    public User toUser(RegisterRequest request) {
        User user = new User();
        user.setEmail(normalizeEmail(request.getEmail()));
        user.setName(request.getName().trim());
        user.setReferralCodeUsed(blankToNull(request.getReferralCode()));
        return user;
    }

    public AuthResponse toAuthResponse(User user, String token) {
        return AuthResponse.from(user, token);
    }

    private static String normalizeEmail(String email) {
        return email.trim().toLowerCase();
    }

    private static String blankToNull(String s) {
        if (s == null) {
            return null;
        }
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }
}
