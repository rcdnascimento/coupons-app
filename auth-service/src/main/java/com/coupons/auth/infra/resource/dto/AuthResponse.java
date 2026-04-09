package com.coupons.auth.infra.resource.dto;

import com.coupons.auth.domain.entity.User;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class AuthResponse {

    private final String token;
    private final UUID userId;
    private final String email;
    private final String name;
    private final List<String> roles;

    @JsonCreator
    public AuthResponse(
            @JsonProperty("token") String token,
            @JsonProperty("userId") UUID userId,
            @JsonProperty("email") String email,
            @JsonProperty("name") String name,
            @JsonProperty("roles") List<String> roles) {
        this.token = token;
        this.userId = userId;
        this.email = email;
        this.name = name;
        this.roles = roles != null ? List.copyOf(roles) : List.of();
    }

    public static AuthResponse from(User user, String token) {
        List<String> r = Collections.singletonList(user.getRole().name());
        return new AuthResponse(token, user.getId(), user.getEmail(), user.getName(), r);
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

    public List<String> getRoles() {
        return roles;
    }
}
