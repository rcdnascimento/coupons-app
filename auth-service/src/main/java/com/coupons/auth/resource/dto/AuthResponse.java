package com.coupons.auth.infra.resource.dto;

import com.coupons.auth.domain.entity.User;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.UUID;

public class AuthResponse {

    private final String token;
    private final UUID userId;
    private final String email;
    private final String name;

    @JsonCreator
    public AuthResponse(
            @JsonProperty("token") String token,
            @JsonProperty("userId") UUID userId,
            @JsonProperty("email") String email,
            @JsonProperty("name") String name) {
        this.token = token;
        this.userId = userId;
        this.email = email;
        this.name = name;
    }

    public static AuthResponse from(User user, String token) {
        return new AuthResponse(token, user.getId(), user.getEmail(), user.getName());
    }

    public String getToken() {
        return token;
    }

    public UUID getUserId() {
        return userId;
    }

    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }
}
