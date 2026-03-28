package com.coupons.ledger.infra.resource.dto;

import com.coupons.ledger.domain.entity.LedgerEntry;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import java.time.Instant;
import java.util.UUID;

@JsonAutoDetect(
        fieldVisibility = JsonAutoDetect.Visibility.ANY,
        getterVisibility = JsonAutoDetect.Visibility.PUBLIC_ONLY,
        isGetterVisibility = JsonAutoDetect.Visibility.NONE)
public class EntryResponse {

    private UUID id;
    private UUID userId;
    private int amount;
    private String reason;
    private String refType;
    private String refId;
    private String idempotencyKey;
    private Instant createdAt;

    public static EntryResponse from(LedgerEntry e) {
        EntryResponse r = new EntryResponse();
        r.id = e.getId();
        r.userId = e.getUserId();
        r.amount = e.getAmount();
        r.reason = e.getReason();
        r.refType = e.getRefType();
        r.refId = e.getRefId();
        r.idempotencyKey = e.getIdempotencyKey();
        r.createdAt = e.getCreatedAt();
        return r;
    }

    public UUID getId() {
        return id;
    }

    public UUID getUserId() {
        return userId;
    }

    public int getAmount() {
        return amount;
    }

    public String getReason() {
        return reason;
    }

    public String getRefType() {
        return refType;
    }

    public String getRefId() {
        return refId;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}

