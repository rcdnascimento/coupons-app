package com.coupons.prizes.infra.persistence;

import com.coupons.prizes.domain.entity.PrizeDelivery;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PrizeDeliveryRepository extends JpaRepository<PrizeDelivery, UUID> {

    boolean existsByCampaignIdAndUserIdAndCouponId(UUID campaignId, UUID userId, UUID couponId);

    List<PrizeDelivery> findByUserIdOrderByProcessedAtDesc(UUID userId);

    List<PrizeDelivery> findByUserIdAndCampaignIdOrderByProcessedAtDesc(UUID userId, UUID campaignId);
}

