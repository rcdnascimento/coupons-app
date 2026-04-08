package com.coupons.campaigns.infra.resource.dto;

/** Estado da inscrição do utilizador na campanha: NONE, PROCESSING, ACTIVE, PAYMENT_FAILED. */
public class MyCampaignSubscriptionResponse {

    private String status;

    public MyCampaignSubscriptionResponse() {}

    public MyCampaignSubscriptionResponse(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
