package com.coupons.bff.infra.resource.dto;

import java.util.UUID;

public class BalanceResponse {

    private UUID userId;
    private int balance;

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public int getBalance() {
        return balance;
    }

    public void setBalance(int balance) {
        this.balance = balance;
    }
}
