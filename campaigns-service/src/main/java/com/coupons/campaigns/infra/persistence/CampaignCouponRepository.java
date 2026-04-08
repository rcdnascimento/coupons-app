package com.coupons.campaigns.infra.persistence;

import com.coupons.campaigns.domain.entity.CampaignCoupon;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CampaignCouponRepository extends JpaRepository<CampaignCoupon, UUID> {

    long countByCampaignId(UUID campaignId);

    boolean existsByCampaignIdAndCouponId(UUID campaignId, UUID couponId);

    @Query(
            "SELECT cc.couponId FROM CampaignCoupon cc WHERE cc.campaignId = :campaignId "
                    + "AND cc.couponId NOT IN (SELECT a.couponId FROM CampaignAllocation a)")
    List<UUID> findLinkedCouponIdsNotYetAllocated(@Param("campaignId") UUID campaignId);

    List<CampaignCoupon> findByCampaignIdOrderByPriorityAsc(UUID campaignId);
}
