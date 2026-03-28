package com.coupons.ledger.domain.service;

import com.coupons.ledger.infra.persistence.LedgerEntryRepository;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LedgerBalanceQueryService {

    private final LedgerEntryRepository ledgerEntryRepository;

    public LedgerBalanceQueryService(LedgerEntryRepository ledgerEntryRepository) {
        this.ledgerEntryRepository = ledgerEntryRepository;
    }

    @Transactional(readOnly = true)
    public int currentBalance(UUID userId) {
        Integer balance = ledgerEntryRepository.getBalanceByUserId(userId);
        return balance == null ? 0 : balance;
    }
}
