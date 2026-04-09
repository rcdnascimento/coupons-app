package com.coupons.bff.infra.resource.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

public class AddCouponToCampaignRequest {

    @NotBlank
    @Size(max = 128)
    private String code;

    @Size(max = 255)
    private String title;

    private Integer priority;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }
}
