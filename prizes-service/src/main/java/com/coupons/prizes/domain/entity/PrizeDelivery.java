package com.coupons.prizes.domain.entity;

import java.time.Instant;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.Convert;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import com.coupons.prizes.infra.persistence.UuidChar36Converter;
import org.hibernate.annotations.Type;

@Entity
@Table(
        name = "prize_deliveries",
        uniqueConstraints = {
            @UniqueConstraint(
                    name = "uk_prize_delivery_business",
                    columnNames = {"campaign_id", "user_id", "coupon_id"})
        })
public class PrizeDelivery {

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
    @Column(name = "coupon_id", nullable = false, length = 36, columnDefinition = "CHAR(36)")
    private UUID couponId;

    @Column(name = "coupon_code", nullable = false, length = 128)
    private String couponCode;

    @Column(name = "payload_snapshot", nullable = false, columnDefinition = "TEXT")
    private String payloadSnapshot;

    @Column(nullable = false, length = 32)
    private String status;

    @Column(name = "correlation_id", length = 128)
    private String correlationId;

    @Column(name = "kafka_partition")
    private Integer kafkaPartition;

    @Column(name = "kafka_offset")
    private Long kafkaOffset;

    @Column(name = "processed_at", nullable = false)
    private Instant processedAt;

    @PrePersist
    void prePersist() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (processedAt == null) {
            processedAt = Instant.now();
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

    public String getCouponCode() {
        return couponCode;
    }

    public void setCouponCode(String couponCode) {
        this.couponCode = couponCode;
    }

    public String getPayloadSnapshot() {
        return payloadSnapshot;
    }

    public void setPayloadSnapshot(String payloadSnapshot) {
        this.payloadSnapshot = payloadSnapshot;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }

    public Integer getKafkaPartition() {
        return kafkaPartition;
    }

    public void setKafkaPartition(Integer kafkaPartition) {
        this.kafkaPartition = kafkaPartition;
    }

    public Long getKafkaOffset() {
        return kafkaOffset;
    }

    public void setKafkaOffset(Long kafkaOffset) {
        this.kafkaOffset = kafkaOffset;
    }

    public Instant getProcessedAt() {
        return processedAt;
    }

    public void setProcessedAt(Instant processedAt) {
        this.processedAt = processedAt;
    }
}

