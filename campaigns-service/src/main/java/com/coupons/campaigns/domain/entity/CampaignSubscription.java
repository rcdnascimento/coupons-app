package com.coupons.campaigns.domain.entity;

import com.coupons.campaigns.domain.CampaignSubscriptionStatus;
import java.time.Instant;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import com.coupons.campaigns.infra.persistence.UuidChar36Converter;
import org.hibernate.annotations.Type;

@Entity
@Table(
        name = "campaign_subscriptions",
        uniqueConstraints =
                @UniqueConstraint(name = "uk_campaign_subscriptions", columnNames = {"campaign_id", "user_id"}))
public class CampaignSubscription {

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

    @Column(name = "subscribed_at", nullable = false)
    private Instant subscribedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "subscription_status", nullable = false, length = 32)
    private CampaignSubscriptionStatus status = CampaignSubscriptionStatus.PROCESSING;

    @PrePersist
    void prePersist() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (subscribedAt == null) {
            subscribedAt = Instant.now();
        }
        if (status == null) {
            status = CampaignSubscriptionStatus.PROCESSING;
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

    public Instant getSubscribedAt() {
        return subscribedAt;
    }

    public void setSubscribedAt(Instant subscribedAt) {
        this.subscribedAt = subscribedAt;
    }

    public CampaignSubscriptionStatus getStatus() {
        return status;
    }

    public void setStatus(CampaignSubscriptionStatus status) {
        this.status = status;
    }
}
