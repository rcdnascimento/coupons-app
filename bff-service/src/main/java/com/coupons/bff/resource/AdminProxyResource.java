package com.coupons.bff.resource;

import com.coupons.bff.infra.gateway.auth.AuthGateway;
import com.coupons.bff.infra.gateway.ledger.LedgerGateway;
import com.coupons.bff.infra.resource.dto.AdminUserSearchResponse;
import com.coupons.bff.infra.resource.dto.LedgerEntryRequest;
import com.coupons.bff.infra.resource.dto.LedgerEntryResponse;
import java.util.List;
import javax.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
public class AdminProxyResource {

    private final LedgerGateway ledgerGateway;
    private final AuthGateway authGateway;

    public AdminProxyResource(LedgerGateway ledgerGateway, AuthGateway authGateway) {
        this.ledgerGateway = ledgerGateway;
        this.authGateway = authGateway;
    }

    @GetMapping(value = "/users/search", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<AdminUserSearchResponse> searchUsers(@RequestParam(value = "q", required = false) String q) {
        return authGateway.searchUsers(q == null ? "" : q);
    }

    @PostMapping(
            value = "/ledger/credits",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<LedgerEntryResponse> creditLedger(@Valid @RequestBody LedgerEntryRequest body) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ledgerGateway.credit(body));
    }
}
