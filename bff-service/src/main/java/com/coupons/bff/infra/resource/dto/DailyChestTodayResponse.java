package com.coupons.bff.infra.resource.dto;

import java.time.Instant;
import java.time.LocalDate;

public class DailyChestTodayResponse {

    private boolean openedToday;
    private Integer rewardCoins;
    private LocalDate localDate;
    private Instant openedAt;
    private Boolean alreadyOpened;

    public boolean isOpenedToday() {
        return openedToday;
    }

    public void setOpenedToday(boolean openedToday) {
        this.openedToday = openedToday;
    }

    public Integer getRewardCoins() {
        return rewardCoins;
    }

    public void setRewardCoins(Integer rewardCoins) {
        this.rewardCoins = rewardCoins;
    }

    public LocalDate getLocalDate() {
        return localDate;
    }

    public void setLocalDate(LocalDate localDate) {
        this.localDate = localDate;
    }

    public Instant getOpenedAt() {
        return openedAt;
    }

    public void setOpenedAt(Instant openedAt) {
        this.openedAt = openedAt;
    }

    public Boolean getAlreadyOpened() {
        return alreadyOpened;
    }

    public void setAlreadyOpened(Boolean alreadyOpened) {
        this.alreadyOpened = alreadyOpened;
    }
}
