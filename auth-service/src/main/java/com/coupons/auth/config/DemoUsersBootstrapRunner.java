package com.coupons.auth.config;

import com.coupons.auth.domain.entity.User;
import com.coupons.auth.domain.entity.UserRole;
import com.coupons.auth.infra.gateway.profile.ProfileGateway;
import com.coupons.auth.infra.persistence.UserRepository;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Seed idempotente de utilizadores comuns para demonstração (role {@link UserRole#USER}).
 */
@Component
@Order(2)
public class DemoUsersBootstrapRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DemoUsersBootstrapRunner.class);
    private static final String DEFAULT_TIMEZONE = "America/Sao_Paulo";
    private static final int PROFILE_MAX_ATTEMPTS = 12;
    private static final long PROFILE_RETRY_MS = 2_000L;

    private static final List<SeedUser> DEFAULT_USERS =
            List.of(
                    new SeedUser("richard@gmail.com", "Richard", "password"),
                    new SeedUser("lucas@gmail.com", "Lucas", "password"));

    private final boolean enabled;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ProfileGateway profileGateway;
    private final TransactionTemplate transactionTemplate;

    public DemoUsersBootstrapRunner(
            @Value("${auth.bootstrap-demo-users.enabled:true}") boolean enabled,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            ProfileGateway profileGateway,
            TransactionTemplate transactionTemplate) {
        this.enabled = enabled;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.profileGateway = profileGateway;
        this.transactionTemplate = transactionTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!enabled) {
            return;
        }
        for (SeedUser spec : DEFAULT_USERS) {
            Seeded created = ensureUser(spec);
            if (created != null) {
                ensureProfile(created.userId, created.email, created.name);
            }
        }
    }

    private Seeded ensureUser(SeedUser spec) {
        return transactionTemplate.execute(
                status -> {
                    String email = spec.email.trim().toLowerCase(Locale.ROOT);
                    User user =
                            userRepository
                                    .findByEmail(email)
                                    .map(
                                            existing -> {
                                                existing.setPasswordHash(
                                                        passwordEncoder.encode(spec.password));
                                                if (existing.getRole() != UserRole.USER) {
                                                    existing.setRole(UserRole.USER);
                                                }
                                                return userRepository.save(existing);
                                            })
                                    .orElseGet(
                                            () -> {
                                                User created = new User();
                                                created.setEmail(email);
                                                created.setName(spec.name);
                                                created.setPasswordHash(
                                                        passwordEncoder.encode(spec.password));
                                                created.setRole(UserRole.USER);
                                                User saved = userRepository.save(created);
                                                log.info(
                                                        "Utilizador comum de seed criado: {} ({})",
                                                        email,
                                                        spec.name);
                                                return saved;
                                            });
                    return new Seeded(user.getId(), user.getEmail(), user.getName());
                });
    }

    private void ensureProfile(UUID userId, String email, String name) {
        for (int attempt = 1; attempt <= PROFILE_MAX_ATTEMPTS; attempt++) {
            try {
                profileGateway.createProfile(userId, name, DEFAULT_TIMEZONE, null);
                log.info("Perfil do utilizador de seed garantido para {}", email);
                return;
            } catch (Exception ex) {
                log.warn(
                        "Tentativa {}/{} de criar perfil de {} falhou: {}",
                        attempt,
                        PROFILE_MAX_ATTEMPTS,
                        email,
                        ex.getMessage());
                if (attempt == PROFILE_MAX_ATTEMPTS) {
                    log.error(
                            "Utilizador {} criado no auth, mas o perfil não foi criado. "
                                    + "Reinicie o auth-service após o profile ficar healthy.",
                            email);
                    return;
                }
                try {
                    Thread.sleep(PROFILE_RETRY_MS);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }
    }

    private static final class SeedUser {
        private final String email;
        private final String name;
        private final String password;

        private SeedUser(String email, String name, String password) {
            this.email = email;
            this.name = name;
            this.password = password;
        }
    }

    private static final class Seeded {
        private final UUID userId;
        private final String email;
        private final String name;

        private Seeded(UUID userId, String email, String name) {
            this.userId = userId;
            this.email = email;
            this.name = name;
        }
    }
}
