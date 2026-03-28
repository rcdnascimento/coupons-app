package com.coupons.campaigns.domain;

public enum CampaignSubscriptionStatus {
    /** Débito em fila ou a ser processado pelo ledger. */
    PROCESSING,
    /** Pagamento confirmado no ledger. */
    ACTIVE,
    /** Débito rejeitado (ex.: saldo insuficiente). */
    PAYMENT_FAILED
}
