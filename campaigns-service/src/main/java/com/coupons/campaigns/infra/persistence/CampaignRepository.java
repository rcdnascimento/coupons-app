package com.coupons.campaigns.infra.persistence;

import com.coupons.campaigns.domain.CampaignStatus;
import com.coupons.campaigns.domain.entity.Campaign;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CampaignRepository extends JpaRepository<Campaign, UUID> {

    List<Campaign> findByStatusOrderByCreatedAtDesc(CampaignStatus status);

    List<Campaign> findByStatusAndDistributionAtLessThanEqualOrderByDistributionAtAsc(
            CampaignStatus status, Instant distributionAt);
}
