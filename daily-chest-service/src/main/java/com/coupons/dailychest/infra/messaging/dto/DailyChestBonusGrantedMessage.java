package com.coupons.dailychest.infra.messaging.dto;

import java.time.LocalDate;
import java.util.UUID;

public class DailyChestBonusGrantedMessage {

    private UUID userId;
    private int rewardCoins;
    private LocalDate localDate;
    private String idempotencyKey;
    private int schemaVersion = 1;

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public int getRewardCoins() {
        return rewardCoins;
    }

    public void setRewardCoins(int rewardCoins) {
        this.rewardCoins = rewardCoins;
    }

    public LocalDate getLocalDate() {
        return localDate;
    }

    public void setLocalDate(LocalDate localDate) {
        this.localDate = localDate;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public void setIdempotencyKey(String idempotencyKey) {
        this.idempotencyKey = idempotencyKey;
    }

    public int getSchemaVersion() {
        return schemaVersion;
    }

    public void setSchemaVersion(int schemaVersion) {
        this.schemaVersion = schemaVersion;
    }
}
