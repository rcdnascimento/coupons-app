package com.coupons.bff.resource;

import com.coupons.bff.infra.gateway.auth.AuthGateway;
import com.coupons.bff.infra.resource.dto.AuthTokenResponse;
import com.coupons.bff.infra.resource.dto.LoginRequest;
import com.coupons.bff.infra.resource.dto.RegisterRequest;
import javax.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthProxyResource {

    private final AuthGateway authGateway;

    public AuthProxyResource(AuthGateway authGateway) {
        this.authGateway = authGateway;
    }

    @PostMapping(value = "/register", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AuthTokenResponse> register(@Valid @RequestBody RegisterRequest body) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authGateway.register(body));
    }

    @PostMapping(value = "/login", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AuthTokenResponse> login(@Valid @RequestBody LoginRequest body) {
        return ResponseEntity.ok(authGateway.login(body));
    }
}
