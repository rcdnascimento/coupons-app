package com.coupons.bff.infra.resource.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

public class CreateCompanyRequest {

    @NotBlank
    @Size(max = 255)
    private String name;

    @NotBlank
    @Pattern(regexp = "\\d{14}", message = "cnpj deve conter 14 dígitos numéricos")
    private String cnpj;

    @Size(max = 1024)
    private String logoUrl;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCnpj() {
        return cnpj;
    }

    public void setCnpj(String cnpj) {
        this.cnpj = cnpj;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

    public void setLogoUrl(String logoUrl) {
        this.logoUrl = logoUrl;
    }
}
