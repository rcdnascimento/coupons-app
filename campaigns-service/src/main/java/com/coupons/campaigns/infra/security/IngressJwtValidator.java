package com.coupons.campaigns.infra.security;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Component;

@Component
public class IngressJwtValidator {

    private final SecretKey signingKey;

    public IngressJwtValidator(IngressAuthProperties props) {
        String secret = props.getJwtSecret();
        if (secret == null || secret.isBlank()) {
            this.signingKey = null;
        } else {
            this.signingKey = hmacSha256KeyFromSecret(secret);
        }
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

    public boolean isValidBearerToken(String authorizationHeader) {
        if (signingKey == null) {
            return false;
        }
        if (authorizationHeader == null || !authorizationHeader.regionMatches(true, 0, "Bearer ", 0, 7)) {
            return false;
        }
        String token = authorizationHeader.substring(7).trim();
        if (token.isEmpty()) {
            return false;
        }
        try {
            Jwts.parser().verifyWith(signingKey).build().parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException ex) {
            return false;
        }
    }
}
