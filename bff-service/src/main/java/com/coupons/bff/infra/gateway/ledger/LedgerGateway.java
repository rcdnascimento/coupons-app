package com.coupons.bff.infra.gateway.ledger;

import com.coupons.bff.infra.resource.dto.BalanceResponse;
import com.coupons.bff.infra.resource.dto.LedgerEntryRequest;
import com.coupons.bff.infra.resource.dto.LedgerEntryResponse;

public interface LedgerGateway {

    BalanceResponse getBalance(String userId);

    LedgerEntryResponse credit(LedgerEntryRequest request);
}
