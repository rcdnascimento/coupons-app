package com.coupons.ledger.infra.resource.dto;

import java.util.UUID;

public class BalanceResponse {

    private final UUID userId;
    private final int balance;

    public BalanceResponse(UUID userId, int balance) {
        this.userId = userId;
        this.balance = balance;
    }

    public UUID getUserId() {
        return userId;
    }

    public int getBalance() {
        return balance;
    }
}

