package com.coupons.ledger.infra.messaging.dto;

import java.util.UUID;

/** Consumido a partir do tópico `referral.bonus.granted` (publicado pelo profile-service). */
public class ReferralBonusGrantedMessage {

    private UUID newUserId;
    private UUID referrerUserId;
    private String referralCode;
    private int bonusAmount = 10;
    private int schemaVersion = 1;

    public UUID getNewUserId() {
        return newUserId;
    }

    public void setNewUserId(UUID newUserId) {
        this.newUserId = newUserId;
    }

    public UUID getReferrerUserId() {
        return referrerUserId;
    }

    public void setReferrerUserId(UUID referrerUserId) {
        this.referrerUserId = referrerUserId;
    }

    public String getReferralCode() {
        return referralCode;
    }

    public void setReferralCode(String referralCode) {
        this.referralCode = referralCode;
    }

    public int getBonusAmount() {
        return bonusAmount;
    }

    public void setBonusAmount(int bonusAmount) {
        this.bonusAmount = bonusAmount;
    }

    public int getSchemaVersion() {
        return schemaVersion;
    }

    public void setSchemaVersion(int schemaVersion) {
        this.schemaVersion = schemaVersion;
    }
}
