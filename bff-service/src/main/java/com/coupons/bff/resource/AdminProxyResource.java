package com.coupons.bff.resource;

import com.coupons.bff.infra.gateway.ledger.LedgerGateway;
import com.coupons.bff.infra.resource.dto.LedgerEntryRequest;
import com.coupons.bff.infra.resource.dto.LedgerEntryResponse;
import javax.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
public class AdminProxyResource {

    private final LedgerGateway ledgerGateway;

    public AdminProxyResource(LedgerGateway ledgerGateway) {
        this.ledgerGateway = ledgerGateway;
    }

    @PostMapping(
            value = "/ledger/credits",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<LedgerEntryResponse> creditLedger(@Valid @RequestBody LedgerEntryRequest body) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ledgerGateway.credit(body));
    }
}
