package com.coupons.profile.domain.event;

import java.util.UUID;

/** Publicado após commit da transação; o listener envia o evento ao Kafka para crédito no ledger. */
public class ReferralBonusGrantedEvent {

    private final UUID referrerUserId;
    private final UUID newUserId;
    private final String referralCode;

    public ReferralBonusGrantedEvent(UUID referrerUserId, UUID newUserId, String referralCode) {
        this.referrerUserId = referrerUserId;
        this.newUserId = newUserId;
        this.referralCode = referralCode;
    }

    public UUID getReferrerUserId() {
        return referrerUserId;
    }

    public UUID getNewUserId() {
        return newUserId;
    }

    public String getReferralCode() {
        return referralCode;
    }
}
