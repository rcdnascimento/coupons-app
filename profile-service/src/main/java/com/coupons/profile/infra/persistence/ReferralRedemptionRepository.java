package com.coupons.profile.infra.persistence;

import com.coupons.profile.domain.entity.ReferralRedemption;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReferralRedemptionRepository extends JpaRepository<ReferralRedemption, UUID> {

    boolean existsByReferralCode(String referralCode);
}
