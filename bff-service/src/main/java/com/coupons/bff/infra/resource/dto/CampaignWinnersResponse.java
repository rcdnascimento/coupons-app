package com.coupons.bff.infra.resource.dto;

import java.util.List;

public class CampaignWinnersResponse {

    private List<CampaignWinnerEntry> entries;

    public List<CampaignWinnerEntry> getEntries() {
        return entries;
    }

    public void setEntries(List<CampaignWinnerEntry> entries) {
        this.entries = entries;
    }
}
