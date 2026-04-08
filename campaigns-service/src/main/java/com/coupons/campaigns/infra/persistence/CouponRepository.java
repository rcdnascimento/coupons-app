package com.coupons.campaigns.infra.persistence;

import com.coupons.campaigns.domain.CouponStatus;
import com.coupons.campaigns.domain.entity.Coupon;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CouponRepository extends JpaRepository<Coupon, UUID> {

    boolean existsByCode(String code);

    Optional<Coupon> findByCode(String code);

    @Query(
            "SELECT c FROM Coupon c WHERE (LOWER(c.code) LIKE LOWER(CONCAT('%', CONCAT(:q, '%'))) "
                    + "OR (c.title IS NOT NULL AND LOWER(c.title) LIKE LOWER(CONCAT('%', CONCAT(:q, '%'))))) "
                    + "ORDER BY c.createdAt DESC")
    List<Coupon> searchByCodeOrTitle(@Param("q") String q, Pageable pageable);

    @Query(
            "SELECT c FROM Coupon c WHERE (LOWER(c.code) LIKE LOWER(CONCAT('%', CONCAT(:q, '%'))) "
                    + "OR (c.title IS NOT NULL AND LOWER(c.title) LIKE LOWER(CONCAT('%', CONCAT(:q, '%'))))) "
                    + "AND c.status = :status "
                    + "ORDER BY c.createdAt DESC")
    List<Coupon> searchByCodeOrTitleAndStatus(
            @Param("q") String q, @Param("status") CouponStatus status, Pageable pageable);
}
