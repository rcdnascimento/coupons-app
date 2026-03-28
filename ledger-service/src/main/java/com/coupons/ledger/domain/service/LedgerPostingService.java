package com.coupons.ledger.domain.service;

import com.coupons.ledger.domain.entity.LedgerEntry;
import com.coupons.ledger.infra.persistence.LedgerEntryRepository;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LedgerPostingService {

    private final LedgerEntryRepository ledgerEntryRepository;

    public LedgerPostingService(LedgerEntryRepository ledgerEntryRepository) {
        this.ledgerEntryRepository = ledgerEntryRepository;
    }

    @Transactional
    public LedgerEntry postWithSignedAmount(LedgerEntry line, int signedAmount) {
        String key = line.getIdempotencyKey().trim();
        Optional<LedgerEntry> existing = ledgerEntryRepository.findByIdempotencyKey(key);
        if (existing.isPresent()) {
            return existing.get();
        }
        LedgerEntry entry = new LedgerEntry();
        entry.setUserId(line.getUserId());
        entry.setAmount(signedAmount);
        entry.setReason(line.getReason().trim());
        entry.setRefType(blankToNull(line.getRefType()));
        entry.setRefId(blankToNull(line.getRefId()));
        entry.setIdempotencyKey(key);
        return ledgerEntryRepository.save(entry);
    }

    private static String blankToNull(String v) {
        if (v == null) {
            return null;
        }
        String trimmed = v.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
