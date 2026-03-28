package com.coupons.campaigns.domain.entity;

import java.time.Instant;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Convert;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import com.coupons.campaigns.domain.PrizeDispatchStatus;
import com.coupons.campaigns.infra.persistence.UuidChar36Converter;
import org.hibernate.annotations.Type;

@Entity
@Table(
        name = "campaign_allocations",
        uniqueConstraints = {
            @UniqueConstraint(name = "uk_allocations_coupon", columnNames = "coupon_id"),
            @UniqueConstraint(name = "uk_allocations_campaign_user", columnNames = {"campaign_id", "user_id"})
        })
public class CampaignAllocation {

    @Id
    @Type(type = "uuid-char")
    @Column(name = "id", nullable = false, length = 36, columnDefinition = "CHAR(36)")
    private UUID id;

    @Convert(converter = UuidChar36Converter.class)
    @Column(name = "campaign_id", nullable = false, length = 36, columnDefinition = "CHAR(36)")
    private UUID campaignId;

    @Convert(converter = UuidChar36Converter.class)
    @Column(name = "user_id", nullable = false, length = 36, columnDefinition = "CHAR(36)")
    private UUID userId;

    @Convert(converter = UuidChar36Converter.class)
    @Column(name = "coupon_id", nullable = false, unique = true, length = 36, columnDefinition = "CHAR(36)")
    private UUID couponId;

    @Column(name = "allocated_at", nullable = false)
    private Instant allocatedAt;

    @Column(name = "code_snapshot", length = 128)
    private String codeSnapshot;

    @Enumerated(EnumType.STRING)
    @Column(name = "dispatch_status", length = 32)
    private PrizeDispatchStatus dispatchStatus;

    @Column(name = "dispatch_attempt_count")
    private Integer dispatchAttemptCount;

    @Column(name = "dispatch_last_attempt_at")
    private Instant dispatchLastAttemptAt;

    @Column(name = "dispatch_last_error", length = 500)
    private String dispatchLastError;

    @PrePersist
    void prePersist() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (allocatedAt == null) {
            allocatedAt = Instant.now();
        }
        if (dispatchAttemptCount == null) {
            dispatchAttemptCount = 0;
        }
        if (dispatchStatus == null) {
            dispatchStatus = PrizeDispatchStatus.PENDING;
        }
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getCampaignId() {
        return campaignId;
    }

    public void setCampaignId(UUID campaignId) {
        this.campaignId = campaignId;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public UUID getCouponId() {
        return couponId;
    }

    public void setCouponId(UUID couponId) {
        this.couponId = couponId;
    }

    public Instant getAllocatedAt() {
        return allocatedAt;
    }

    public void setAllocatedAt(Instant allocatedAt) {
        this.allocatedAt = allocatedAt;
    }

    public String getCodeSnapshot() {
        return codeSnapshot;
    }

    public void setCodeSnapshot(String codeSnapshot) {
        this.codeSnapshot = codeSnapshot;
    }

    public PrizeDispatchStatus getDispatchStatus() {
        return dispatchStatus;
    }

    public void setDispatchStatus(PrizeDispatchStatus dispatchStatus) {
        this.dispatchStatus = dispatchStatus;
    }

    public Integer getDispatchAttemptCount() {
        return dispatchAttemptCount;
    }

    public void setDispatchAttemptCount(Integer dispatchAttemptCount) {
        this.dispatchAttemptCount = dispatchAttemptCount;
    }

    public Instant getDispatchLastAttemptAt() {
        return dispatchLastAttemptAt;
    }

    public void setDispatchLastAttemptAt(Instant dispatchLastAttemptAt) {
        this.dispatchLastAttemptAt = dispatchLastAttemptAt;
    }

    public String getDispatchLastError() {
        return dispatchLastError;
    }

    public void setDispatchLastError(String dispatchLastError) {
        this.dispatchLastError = dispatchLastError;
    }
}
