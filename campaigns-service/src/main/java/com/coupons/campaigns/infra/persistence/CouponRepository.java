package com.coupons.campaigns.infra.persistence;

import com.coupons.campaigns.domain.entity.Coupon;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CouponRepository extends JpaRepository<Coupon, UUID> {

    boolean existsByCode(String code);

    Optional<Coupon> findByCode(String code);
}
