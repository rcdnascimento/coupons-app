package com.coupons.profile.domain.entity;

import java.time.Instant;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import org.hibernate.annotations.Type;

@Entity
@Table(name = "referral_redemptions")
public class ReferralRedemption {

    @Id
    @Type(type = "uuid-char")
    @Column(name = "id", nullable = false, length = 36, columnDefinition = "CHAR(36)")
    private UUID id;

    /** Código de indicação consumido (único: só pode ser usado uma vez no registo). */
    @Column(name = "referral_code", nullable = false, unique = true, length = 64)
    private String referralCode;

    @Type(type = "uuid-char")
    @Column(name = "referrer_user_id", nullable = false, length = 36, columnDefinition = "CHAR(36)")
    private UUID referrerUserId;

    @Type(type = "uuid-char")
    @Column(name = "referred_user_id", nullable = false, length = 36, columnDefinition = "CHAR(36)")
    private UUID referredUserId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @PrePersist
    void prePersist() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getReferralCode() {
        return referralCode;
    }

    public void setReferralCode(String referralCode) {
        this.referralCode = referralCode;
    }

    public UUID getReferrerUserId() {
        return referrerUserId;
    }

    public void setReferrerUserId(UUID referrerUserId) {
        this.referrerUserId = referrerUserId;
    }

    public UUID getReferredUserId() {
        return referredUserId;
    }

    public void setReferredUserId(UUID referredUserId) {
        this.referredUserId = referredUserId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
