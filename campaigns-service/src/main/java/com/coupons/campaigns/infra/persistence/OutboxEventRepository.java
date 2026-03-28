package com.coupons.campaigns.infra.persistence;

import com.coupons.campaigns.domain.OutboxStatus;
import com.coupons.campaigns.domain.entity.OutboxEvent;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, UUID> {

    List<OutboxEvent> findTop50ByStatusOrderByCreatedAtAsc(OutboxStatus status);
}

