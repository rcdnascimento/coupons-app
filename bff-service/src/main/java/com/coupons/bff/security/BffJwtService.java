package com.coupons.bff.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

/**
 * Valida JWT emitido pelo auth-service (mesma derivação de chave HMAC que {@code JwtTokenProvider}).
 */
@Component
public class BffJwtService {

    private final SecretKey signingKey;

    public BffJwtService(@Value("${jwt.secret}") String secret) {
        this.signingKey = hmacSha256KeyFromSecret(secret);
    }

    private static SecretKey hmacSha256KeyFromSecret(String secret) {
        try {
            byte[] digest =
                    MessageDigest.getInstance("SHA-256").digest(secret.getBytes(StandardCharsets.UTF_8));
            return Keys.hmacShaKeyFor(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    public Optional<BffUserPrincipal> parseValidToken(String compactJwt) {
        if (compactJwt == null || compactJwt.isBlank()) {
            return Optional.empty();
        }
        try {
            Claims claims = Jwts.parser().verifyWith(signingKey).build().parseSignedClaims(compactJwt.trim()).getPayload();
            UUID userId = UUID.fromString(claims.getSubject());
            String email = claims.get("email", String.class);
            Collection<GrantedAuthority> authorities = authoritiesFromClaims(claims);
            return Optional.of(new BffUserPrincipal(userId, email, authorities));
        } catch (JwtException | IllegalArgumentException ex) {
            return Optional.empty();
        }
    }

    @SuppressWarnings("unchecked")
    private static Collection<GrantedAuthority> authoritiesFromClaims(Claims claims) {
        Object raw = claims.get("roles");
        List<String> names = new ArrayList<>();
        if (raw instanceof List) {
            for (Object o : (List<?>) raw) {
                if (o != null) {
                    names.add(o.toString());
                }
            }
        }
        if (names.isEmpty()) {
            names.add("USER");
        }
        List<GrantedAuthority> out = new ArrayList<>();
        for (String n : names) {
            String upper = n.trim().toUpperCase();
            if (!upper.startsWith("ROLE_")) {
                upper = "ROLE_" + upper;
            }
            out.add(new SimpleGrantedAuthority(upper));
        }
        return Collections.unmodifiableList(out);
    }
}
