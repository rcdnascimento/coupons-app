package com.coupons.bff.infra.resource.dto;

import java.util.List;
import java.util.UUID;

public class CampaignSummaryResponse {

    private UUID campaignId;
    private List<PossiblePrizeResponse> possiblePrizes;

    public UUID getCampaignId() {
        return campaignId;
    }

    public void setCampaignId(UUID campaignId) {
        this.campaignId = campaignId;
    }

    public List<PossiblePrizeResponse> getPossiblePrizes() {
        return possiblePrizes;
    }

    public void setPossiblePrizes(List<PossiblePrizeResponse> possiblePrizes) {
        this.possiblePrizes = possiblePrizes;
    }

    public static class PossiblePrizeResponse {
        private String title;
        private long quantity;

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public long getQuantity() {
            return quantity;
        }

        public void setQuantity(long quantity) {
            this.quantity = quantity;
        }
    }
}
