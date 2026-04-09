package com.coupons.auth.resource;

import com.coupons.auth.domain.entity.User;
import com.coupons.auth.domain.service.AuthLoginService;
import com.coupons.auth.domain.service.AuthRegistrationService;
import com.coupons.auth.infra.resource.dto.AuthResponse;
import com.coupons.auth.infra.resource.dto.LoginRequest;
import com.coupons.auth.infra.resource.dto.RegisterRequest;
import com.coupons.auth.infra.resource.mapper.AuthRestMapper;
import com.coupons.auth.infra.security.JwtTokenProvider;
import javax.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/auth")
public class AuthResource {

    private final AuthRegistrationService authRegistrationService;
    private final AuthLoginService authLoginService;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthRestMapper authRestMapper;

    public AuthResource(
            AuthRegistrationService authRegistrationService,
            AuthLoginService authLoginService,
            JwtTokenProvider jwtTokenProvider,
            AuthRestMapper authRestMapper) {
        this.authRegistrationService = authRegistrationService;
        this.authLoginService = authLoginService;
        this.jwtTokenProvider = jwtTokenProvider;
        this.authRestMapper = authRestMapper;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        User user = authRegistrationService.register(authRestMapper.toUser(request), request.getPassword());
        String token = jwtTokenProvider.createToken(user.getId(), user.getEmail(), user.getRole());
        return authRestMapper.toAuthResponse(user, token);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        User user = authLoginService.authenticate(request.getEmail(), request.getPassword());
        String token = jwtTokenProvider.createToken(user.getId(), user.getEmail(), user.getRole());
        return authRestMapper.toAuthResponse(user, token);
    }
}
