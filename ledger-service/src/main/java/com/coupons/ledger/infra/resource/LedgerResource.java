package com.coupons.ledger.infra.resource;

import com.coupons.ledger.domain.service.LedgerBalanceQueryService;
import com.coupons.ledger.domain.service.LedgerCreditService;
import com.coupons.ledger.domain.service.LedgerDebitService;
import com.coupons.ledger.infra.resource.dto.BalanceResponse;
import com.coupons.ledger.infra.resource.dto.EntryRequest;
import com.coupons.ledger.infra.resource.dto.EntryResponse;
import com.coupons.ledger.infra.resource.mapper.LedgerRestMapper;
import java.util.UUID;
import javax.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/ledger")
public class LedgerResource {

    private final LedgerCreditService ledgerCreditService;
    private final LedgerDebitService ledgerDebitService;
    private final LedgerBalanceQueryService ledgerBalanceQueryService;
    private final LedgerRestMapper ledgerRestMapper;

    public LedgerResource(
            LedgerCreditService ledgerCreditService,
            LedgerDebitService ledgerDebitService,
            LedgerBalanceQueryService ledgerBalanceQueryService,
            LedgerRestMapper ledgerRestMapper) {
        this.ledgerCreditService = ledgerCreditService;
        this.ledgerDebitService = ledgerDebitService;
        this.ledgerBalanceQueryService = ledgerBalanceQueryService;
        this.ledgerRestMapper = ledgerRestMapper;
    }

    @PostMapping("/credit")
    @ResponseStatus(HttpStatus.CREATED)
    public EntryResponse credit(@Valid @RequestBody EntryRequest request) {
        return ledgerRestMapper.toResponse(ledgerCreditService.credit(ledgerRestMapper.toLedgerLine(request)));
    }

    @PostMapping("/debit")
    @ResponseStatus(HttpStatus.CREATED)
    public EntryResponse debit(@Valid @RequestBody EntryRequest request) {
        return ledgerRestMapper.toResponse(ledgerDebitService.debit(ledgerRestMapper.toLedgerLine(request)));
    }

    @GetMapping("/balance/{userId}")
    public BalanceResponse balance(@PathVariable UUID userId) {
        return ledgerRestMapper.toBalanceResponse(userId, ledgerBalanceQueryService.currentBalance(userId));
    }
}
