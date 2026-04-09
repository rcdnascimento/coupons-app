package com.coupons.campaigns.infra.resource.dto;

import com.coupons.campaigns.domain.entity.Company;
import java.time.Instant;
import java.util.UUID;

public class CompanyResponse {

    private UUID id;
    private String name;
    private String cnpj;
    private String logoUrl;
    private Instant createdAt;

    public static CompanyResponse from(Company c) {
        CompanyResponse r = new CompanyResponse();
        r.id = c.getId();
        r.name = c.getName();
        r.cnpj = c.getCnpj();
        r.logoUrl = c.getLogoUrl();
        r.createdAt = c.getCreatedAt();
        return r;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCnpj() {
        return cnpj;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
