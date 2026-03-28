package com.coupons.ledger.domain.service;

import com.coupons.ledger.domain.entity.LedgerEntry;
import com.coupons.ledger.domain.exception.InsufficientBalanceException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LedgerDebitService {

    private final LedgerPostingService ledgerPostingService;
    private final LedgerBalanceQueryService ledgerBalanceQueryService;

    public LedgerDebitService(
            LedgerPostingService ledgerPostingService,
            LedgerBalanceQueryService ledgerBalanceQueryService) {
        this.ledgerPostingService = ledgerPostingService;
        this.ledgerBalanceQueryService = ledgerBalanceQueryService;
    }

    @Transactional
    public LedgerEntry debit(LedgerEntry line) {
        int amount = Math.abs(line.getAmount());
        int balance = ledgerBalanceQueryService.currentBalance(line.getUserId());
        if (balance < amount) {
            throw new InsufficientBalanceException();
        }
        return ledgerPostingService.postWithSignedAmount(line, -amount);
    }
}
