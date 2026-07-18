package com.coupons.auth.config;

import com.coupons.auth.domain.entity.User;
import com.coupons.auth.domain.entity.UserRole;
import com.coupons.auth.infra.gateway.profile.ProfileGateway;
import com.coupons.auth.infra.persistence.UserRepository;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Stream;
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
 * Garante um utilizador ADMIN de seed no arranque (criação idempotente) e promove e-mails
 * listados em {@code auth.bootstrap-admin-emails} quando as contas já existem.
 *
 * <p>Não há Flyway neste serviço: este runner funciona como seed de desenvolvimento/demo.
 */
@Component
@Order(1)
public class AdminRoleBootstrapRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminRoleBootstrapRunner.class);
    private static final String DEFAULT_TIMEZONE = "America/Sao_Paulo";
    private static final int PROFILE_MAX_ATTEMPTS = 12;
    private static final long PROFILE_RETRY_MS = 2_000L;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ProfileGateway profileGateway;
    private final TransactionTemplate transactionTemplate;

    @Value("${auth.bootstrap-admin.enabled:true}")
    private boolean seedEnabled;

    @Value("${auth.bootstrap-admin.email:admin@coupons.local}")
    private String seedEmail;

    @Value("${auth.bootstrap-admin.password:admin123}")
    private String seedPassword;

    @Value("${auth.bootstrap-admin.name:Admin}")
    private String seedName;

    @Value("${auth.bootstrap-admin-emails:}")
    private String bootstrapAdminEmails;

    public AdminRoleBootstrapRunner(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            ProfileGateway profileGateway,
            TransactionTemplate transactionTemplate) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.profileGateway = profileGateway;
        this.transactionTemplate = transactionTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (seedEnabled) {
            SeedAdmin seed = ensureSeedAdmin();
            if (seed != null) {
                ensureProfile(seed.userId, seed.email, seed.name);
            }
        }
        promoteListedEmails();
    }

    private SeedAdmin ensureSeedAdmin() {
        if (seedEmail == null || seedEmail.isBlank() || seedPassword == null || seedPassword.isBlank()) {
            log.warn("Bootstrap de admin ignorado: e-mail ou senha vazios");
            return null;
        }

        return transactionTemplate.execute(
                status -> {
                    String email = seedEmail.trim().toLowerCase(Locale.ROOT);
                    User user =
                            userRepository
                                    .findByEmail(email)
                                    .orElseGet(
                                            () -> {
                                                User created = new User();
                                                created.setEmail(email);
                                                created.setName(
                                                        seedName == null || seedName.isBlank()
                                                                ? "Admin"
                                                                : seedName.trim());
                                                created.setPasswordHash(
                                                        passwordEncoder.encode(seedPassword));
                                                created.setRole(UserRole.ADMIN);
                                                User saved = userRepository.save(created);
                                                log.info("Utilizador ADMIN de seed criado: {}", email);
                                                return saved;
                                            });

                    if (user.getRole() != UserRole.ADMIN) {
                        user.setRole(UserRole.ADMIN);
                        userRepository.save(user);
                        log.info("Utilizador {} promovido a ADMIN", email);
                    }

                    return new SeedAdmin(user.getId(), user.getEmail(), user.getName());
                });
    }

    private void ensureProfile(UUID userId, String email, String name) {
        for (int attempt = 1; attempt <= PROFILE_MAX_ATTEMPTS; attempt++) {
            try {
                profileGateway.createProfile(userId, name, DEFAULT_TIMEZONE, null);
                log.info("Perfil do ADMIN de seed garantido para {}", email);
                return;
            } catch (Exception ex) {
                log.warn(
                        "Tentativa {}/{} de criar perfil do admin falhou: {}",
                        attempt,
                        PROFILE_MAX_ATTEMPTS,
                        ex.getMessage());
                if (attempt == PROFILE_MAX_ATTEMPTS) {
                    log.error(
                            "ADMIN {} criado no auth, mas o perfil não foi criado. "
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

    private void promoteListedEmails() {
        if (bootstrapAdminEmails == null || bootstrapAdminEmails.isBlank()) {
            return;
        }
        transactionTemplate.executeWithoutResult(
                status ->
                        Stream.of(bootstrapAdminEmails.split(","))
                                .map(String::trim)
                                .filter(s -> !s.isEmpty())
                                .map(s -> s.toLowerCase(Locale.ROOT))
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
                                                                        log.info(
                                                                                "Utilizador {} promovido a ADMIN",
                                                                                email);
                                                                    }
                                                                })));
    }

    private static final class SeedAdmin {
        private final UUID userId;
        private final String email;
        private final String name;

        private SeedAdmin(UUID userId, String email, String name) {
            this.userId = userId;
            this.email = email;
            this.name = name;
        }
    }
}
