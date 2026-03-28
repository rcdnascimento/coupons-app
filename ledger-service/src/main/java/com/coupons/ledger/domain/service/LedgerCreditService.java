package com.coupons.ledger.domain.service;

import com.coupons.ledger.domain.entity.LedgerEntry;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LedgerCreditService {

    private final LedgerPostingService ledgerPostingService;

    public LedgerCreditService(LedgerPostingService ledgerPostingService) {
        this.ledgerPostingService = ledgerPostingService;
    }

    @Transactional
    public LedgerEntry credit(LedgerEntry line) {
        return ledgerPostingService.postWithSignedAmount(line, Math.abs(line.getAmount()));
    }
}
