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
@Table(name = "profiles")
public class Profile {

    @Id
    @Type(type = "uuid-char")
    @Column(name = "user_id", nullable = false, updatable = false, length = 36, columnDefinition = "CHAR(36)")
    private UUID userId;

    @Column(name = "display_name", nullable = false, length = 255)
    private String displayName;

    @Column(name = "referral_code", nullable = false, unique = true, length = 64)
    private String referralCode;

    @Column(name = "timezone", length = 64)
    private String timezone;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @PrePersist
    void prePersist() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getReferralCode() {
        return referralCode;
    }

    public void setReferralCode(String referralCode) {
        this.referralCode = referralCode;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
