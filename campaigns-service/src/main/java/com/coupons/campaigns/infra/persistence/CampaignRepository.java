package com.coupons.campaigns.infra.persistence;

import com.coupons.campaigns.domain.CampaignStatus;
import com.coupons.campaigns.domain.entity.Campaign;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface CampaignRepository extends JpaRepository<Campaign, UUID> {

    /**
     * ACTIVE primeiro (ainda em curso), depois por data de distribuição crescente.
     */
    @Query(
            "SELECT c FROM Campaign c ORDER BY CASE WHEN c.status = com.coupons.campaigns.domain.CampaignStatus.ACTIVE"
                    + " THEN 0 ELSE 1 END, c.distributionAt ASC")
    List<Campaign> findAllForListOrderByActiveAndDistribution();

    List<Campaign> findByStatusOrderByCreatedAtDesc(CampaignStatus status);

    List<Campaign> findByStatusAndDistributionAtLessThanEqualOrderByDistributionAtAsc(
            CampaignStatus status, Instant distributionAt);
}
