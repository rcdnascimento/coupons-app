package com.coupons.ledger.domain.exception;

public class InsufficientBalanceException extends RuntimeException {

    public InsufficientBalanceException() {
        super("Saldo insuficiente para realizar o débito");
    }
}

