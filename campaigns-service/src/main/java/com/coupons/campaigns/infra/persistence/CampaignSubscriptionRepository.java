package com.coupons.campaigns.infra.persistence;

import com.coupons.campaigns.domain.CampaignSubscriptionStatus;
import com.coupons.campaigns.domain.entity.CampaignSubscription;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CampaignSubscriptionRepository extends JpaRepository<CampaignSubscription, UUID> {

    Optional<CampaignSubscription> findByCampaignIdAndUserId(UUID campaignId, UUID userId);

    boolean existsByCampaignIdAndUserIdAndStatus(
            UUID campaignId, UUID userId, CampaignSubscriptionStatus status);

    List<CampaignSubscription> findByCampaignIdAndStatusOrderBySubscribedAtAsc(
            UUID campaignId, CampaignSubscriptionStatus status);
}
