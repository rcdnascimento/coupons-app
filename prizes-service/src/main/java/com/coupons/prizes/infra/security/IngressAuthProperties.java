package com.coupons.prizes.infra.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "coupons.ingress")
public class IngressAuthProperties {

    /** Cabeçalho partilhado por BFF e auth-service; obrigatório em todos os pedidos aos microsserviços. */
    private String internalApiKey = "";

    /** Mesmo segredo que auth-service / BFF para validar JWT de utilizadores. */
    private String jwtSecret = "";

    /** Só no auth-service: permite POST /v1/auth/register e /v1/auth/login sem chave nem JWT. */
    private boolean allowPublicAuthRegisterAndLogin = false;

    public String getInternalApiKey() {
        return internalApiKey;
    }

    public void setInternalApiKey(String internalApiKey) {
        this.internalApiKey = internalApiKey;
    }

    public String getJwtSecret() {
        return jwtSecret;
    }

    public void setJwtSecret(String jwtSecret) {
        this.jwtSecret = jwtSecret;
    }

    public boolean isAllowPublicAuthRegisterAndLogin() {
        return allowPublicAuthRegisterAndLogin;
    }

    public void setAllowPublicAuthRegisterAndLogin(boolean allowPublicAuthRegisterAndLogin) {
        this.allowPublicAuthRegisterAndLogin = allowPublicAuthRegisterAndLogin;
    }
}
