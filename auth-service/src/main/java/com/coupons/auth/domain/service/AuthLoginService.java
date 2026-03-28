package com.coupons.auth.domain.service;

import com.coupons.auth.domain.entity.User;
import com.coupons.auth.domain.exception.InvalidCredentialsException;
import com.coupons.auth.infra.persistence.UserRepository;
import java.util.Optional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthLoginService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthLoginService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public User authenticate(String email, String rawPassword) {
        String normalized = normalizeEmail(email);
        Optional<User> userOpt = userRepository.findByEmail(normalized);
        if (!userOpt.isPresent()
                || !passwordEncoder.matches(rawPassword, userOpt.get().getPasswordHash())) {
            throw new InvalidCredentialsException();
        }
        return userOpt.get();
    }

    private static String normalizeEmail(String email) {
        return email.trim().toLowerCase();
    }
}
