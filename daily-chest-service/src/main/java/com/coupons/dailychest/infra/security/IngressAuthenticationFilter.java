package com.coupons.dailychest.infra.security;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Exige {@code X-Internal-Api-Key} (tráfego BFF/auth) ou {@code Authorization: Bearer} JWT válido.
 * Impede acesso anónimo às APIs REST dos microsserviços quando expostas na rede.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class IngressAuthenticationFilter extends OncePerRequestFilter {

    static final String INTERNAL_API_KEY_HEADER = "X-Internal-Api-Key";

    private final IngressAuthProperties properties;
    private final IngressJwtValidator jwtValidator;

    public IngressAuthenticationFilter(IngressAuthProperties properties, IngressJwtValidator jwtValidator) {
        this.properties = properties;
        this.jwtValidator = jwtValidator;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }
        String uri = request.getRequestURI();
        if (uri.startsWith("/actuator/health") || uri.startsWith("/actuator/info")) {
            filterChain.doFilter(request, response);
            return;
        }

        if (properties.isAllowPublicAuthRegisterAndLogin()
                && "POST".equalsIgnoreCase(request.getMethod())
                && (uri.endsWith("/v1/auth/register") || uri.endsWith("/v1/auth/login"))) {
            filterChain.doFilter(request, response);
            return;
        }

        String configuredKey = properties.getInternalApiKey();
        if (configuredKey != null && !configuredKey.isBlank()) {
            String provided = request.getHeader(INTERNAL_API_KEY_HEADER);
            if (constantTimeEquals(configuredKey, provided)) {
                filterChain.doFilter(request, response);
                return;
            }
        }

        String authz = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (jwtValidator.isValidBearerToken(authz)) {
            filterChain.doFilter(request, response);
            return;
        }

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write("{\"error\":\"Não autenticado\"}");
    }

    private static boolean constantTimeEquals(String expected, String actual) {
        if (expected == null || actual == null) {
            return false;
        }
        try {
            return MessageDigest.isEqual(
                    expected.getBytes(StandardCharsets.UTF_8), actual.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            return false;
        }
    }
}
