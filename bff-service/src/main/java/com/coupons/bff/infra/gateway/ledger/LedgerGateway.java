package com.coupons.bff.infra.gateway.ledger;

import com.coupons.bff.infra.resource.dto.BalanceResponse;

public interface LedgerGateway {

    BalanceResponse getBalance(String userId);
}
