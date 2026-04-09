package com.coupons.campaigns.infra.persistence;

import com.coupons.campaigns.domain.entity.CampaignAllocation;
import java.util.UUID;
import java.time.Instant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CampaignAllocationRepository extends JpaRepository<CampaignAllocation, UUID> {

    List<CampaignAllocation> findByCampaignId(UUID campaignId);

    long countByCampaignId(UUID campaignId);

    boolean existsByCampaignIdAndUserId(UUID campaignId, UUID userId);

    boolean existsByCouponId(UUID couponId);

    List<CampaignAllocation>
            findTop200ByDispatchAttemptCountLessThanEqualAndDispatchLastAttemptAtLessThanEqualOrderByDispatchLastAttemptAtAsc(
                    Integer maxAttempts, Instant eligibleBefore);
}
