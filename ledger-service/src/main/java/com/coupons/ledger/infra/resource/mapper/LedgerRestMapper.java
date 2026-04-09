package com.coupons.ledger.infra.resource.mapper;

import com.coupons.ledger.domain.entity.LedgerEntry;
import com.coupons.ledger.infra.resource.dto.BalanceResponse;
import com.coupons.ledger.infra.resource.dto.EntryRequest;
import com.coupons.ledger.infra.resource.dto.EntryResponse;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class LedgerRestMapper {

    public LedgerEntry toLedgerLine(EntryRequest request) {
        LedgerEntry line = new LedgerEntry();
        line.setUserId(request.getUserId());
        line.setAmount(Math.abs(request.getAmount()));
        line.setReason(request.getReason().trim());
        line.setRefType(blankToNull(request.getRefType()));
        line.setRefId(blankToNull(request.getRefId()));
        line.setIdempotencyKey(request.getIdempotencyKey().trim());
        return line;
    }

    public LedgerEntry toLedgerLineFromSubscriptionDebit(
            UUID userId, int amount, String idempotencyKey, UUID campaignId) {
        LedgerEntry line = new LedgerEntry();
        line.setUserId(userId);
        line.setAmount(Math.abs(amount));
        line.setReason("CAMPAIGN_SUBSCRIPTION");
        line.setRefType("CAMPAIGN");
        line.setRefId(campaignId.toString());
        line.setIdempotencyKey(idempotencyKey.trim());
        return line;
    }

    /** Crédito por bónus de indicação; {@code otherPartyUserId} identifica o outro lado (referidor ou indicado). */
    public LedgerEntry toReferralBonusLine(
            UUID userId, int amount, String idempotencyKey, UUID otherPartyUserId) {
        LedgerEntry line = new LedgerEntry();
        line.setUserId(userId);
        line.setAmount(Math.abs(amount));
        line.setReason("REFERRAL_BONUS");
        line.setRefType("REFERRAL_SIGNUP");
        line.setRefId(otherPartyUserId.toString());
        line.setIdempotencyKey(idempotencyKey.trim());
        return line;
    }

    public LedgerEntry toSignupBonusLine(UUID userId, int amount, String idempotencyKey) {
        LedgerEntry line = new LedgerEntry();
        line.setUserId(userId);
        line.setAmount(Math.abs(amount));
        line.setReason("SIGNUP_BONUS");
        line.setRefType("SIGNUP");
        line.setRefId(userId.toString());
        line.setIdempotencyKey(idempotencyKey.trim());
        return line;
    }

    public LedgerEntry toDailyChestBonusLine(
            UUID userId, int amount, String idempotencyKey, LocalDate localDate) {
        LedgerEntry line = new LedgerEntry();
        line.setUserId(userId);
        line.setAmount(Math.abs(amount));
        line.setReason("DAILY_CHEST_BONUS");
        line.setRefType("DAILY_CHEST");
        line.setRefId(localDate.toString());
        line.setIdempotencyKey(idempotencyKey.trim());
        return line;
    }

    public EntryResponse toResponse(LedgerEntry entry) {
        return EntryResponse.from(entry);
    }

    public BalanceResponse toBalanceResponse(UUID userId, int balance) {
        return new BalanceResponse(userId, balance);
    }

    private static String blankToNull(String v) {
        if (v == null) {
            return null;
        }
        String trimmed = v.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
