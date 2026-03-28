package com.coupons.campaigns.domain.entity;

import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Convert;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import com.coupons.campaigns.infra.persistence.UuidChar36Converter;
import org.hibernate.annotations.Type;

@Entity
@Table(
        name = "campaign_coupons",
        uniqueConstraints = @UniqueConstraint(name = "uk_campaign_coupons_coupon", columnNames = "coupon_id"))
public class CampaignCoupon {

    @Id
    @Type(type = "uuid-char")
    @Column(name = "id", nullable = false, length = 36, columnDefinition = "CHAR(36)")
    private UUID id;

    @Convert(converter = UuidChar36Converter.class)
    @Column(name = "campaign_id", nullable = false, length = 36, columnDefinition = "CHAR(36)")
    private UUID campaignId;

    @Convert(converter = UuidChar36Converter.class)
    @Column(name = "coupon_id", nullable = false, unique = true, length = 36, columnDefinition = "CHAR(36)")
    private UUID couponId;

    @Column(name = "priority")
    private Integer priority;

    @PrePersist
    void prePersist() {
        if (id == null) {
            id = UUID.randomUUID();
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

    public UUID getCouponId() {
        return couponId;
    }

    public void setCouponId(UUID couponId) {
        this.couponId = couponId;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }
}
