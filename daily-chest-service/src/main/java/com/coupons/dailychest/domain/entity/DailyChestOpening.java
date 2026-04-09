package com.coupons.dailychest.domain.entity;

import com.coupons.dailychest.infra.persistence.UuidChar36Converter;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import org.hibernate.annotations.Type;

@Entity
@Table(
        name = "daily_chest_openings",
        uniqueConstraints = {
            @UniqueConstraint(
                    name = "uk_daily_chest_opening_user_date",
                    columnNames = {"user_id", "local_date"})
        })
public class DailyChestOpening {

    @Id
    @Type(type = "uuid-char")
    @Column(name = "id", nullable = false, length = 36, columnDefinition = "CHAR(36)")
    private UUID id;

    @Convert(converter = UuidChar36Converter.class)
    @Column(name = "user_id", nullable = false, length = 36, columnDefinition = "CHAR(36)")
    private UUID userId;

    @Column(name = "local_date", nullable = false)
    private LocalDate localDate;

    @Column(name = "timezone", nullable = false, length = 64)
    private String timezone;

    @Column(name = "reward_coins", nullable = false)
    private int rewardCoins;

    @Column(name = "roll_value", nullable = false)
    private int rollValue;

    @Column(name = "idempotency_key", nullable = false, unique = true, length = 255)
    private String idempotencyKey;

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

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public LocalDate getLocalDate() {
        return localDate;
    }

    public void setLocalDate(LocalDate localDate) {
        this.localDate = localDate;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    public int getRewardCoins() {
        return rewardCoins;
    }

    public void setRewardCoins(int rewardCoins) {
        this.rewardCoins = rewardCoins;
    }

    public int getRollValue() {
        return rollValue;
    }

    public void setRollValue(int rollValue) {
        this.rollValue = rollValue;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public void setIdempotencyKey(String idempotencyKey) {
        this.idempotencyKey = idempotencyKey;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
