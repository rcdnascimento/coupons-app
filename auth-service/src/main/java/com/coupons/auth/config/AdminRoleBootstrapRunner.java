package com.coupons.auth.config;

import com.coupons.auth.domain.entity.User;
import com.coupons.auth.domain.entity.UserRole;
import com.coupons.auth.infra.persistence.UserRepository;
import java.util.stream.Stream;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Promove utilizadores existentes a {@link UserRole#ADMIN} conforme e-mails em
 * {@code auth.bootstrap-admin-emails} (lista separada por vírgulas). Útil para o primeiro admin em
 * ambiente novo.
 */
@Component
public class AdminRoleBootstrapRunner implements ApplicationRunner {

    private final UserRepository userRepository;

    @Value("${auth.bootstrap-admin-emails:}")
    private String bootstrapAdminEmails;

    public AdminRoleBootstrapRunner(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (bootstrapAdminEmails == null || bootstrapAdminEmails.isBlank()) {
            return;
        }
        Stream.of(bootstrapAdminEmails.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(String::toLowerCase)
                .distinct()
                .forEach(
                        email ->
                                userRepository
                                        .findByEmail(email)
                                        .ifPresent(
                                                u -> {
                                                    if (u.getRole() != UserRole.ADMIN) {
                                                        u.setRole(UserRole.ADMIN);
                                                        userRepository.save(u);
                                                    }
                                                }));
    }
}
