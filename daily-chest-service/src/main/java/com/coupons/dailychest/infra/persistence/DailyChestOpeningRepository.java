package com.coupons.dailychest.infra.persistence;

import com.coupons.dailychest.domain.entity.DailyChestOpening;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DailyChestOpeningRepository extends JpaRepository<DailyChestOpening, UUID> {

    Optional<DailyChestOpening> findByUserIdAndLocalDate(UUID userId, LocalDate localDate);
}
