package com.coupons.auth.domain.service;

import com.coupons.auth.domain.entity.User;
import com.coupons.auth.domain.exception.EmailAlreadyInUseException;
import com.coupons.auth.infra.gateway.profile.ProfileGateway;
import com.coupons.auth.infra.persistence.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthRegistrationService {

    private static final String DEFAULT_TIMEZONE = "America/Sao_Paulo";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ProfileGateway profileGateway;

    public AuthRegistrationService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            ProfileGateway profileGateway) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.profileGateway = profileGateway;
    }

    @Transactional
    public User register(User newUser, String rawPassword) {
        if (userRepository.existsByEmail(newUser.getEmail())) {
            throw new EmailAlreadyInUseException();
        }
        newUser.setPasswordHash(passwordEncoder.encode(rawPassword));
        userRepository.save(newUser);

        profileGateway.createProfile(
                newUser.getId(), newUser.getName(), DEFAULT_TIMEZONE, newUser.getReferralCodeUsed());

        return newUser;
    }
}
