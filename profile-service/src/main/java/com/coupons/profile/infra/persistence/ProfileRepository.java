package com.coupons.profile.infra.persistence;

import com.coupons.profile.domain.entity.Profile;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProfileRepository extends JpaRepository<Profile, UUID> {

    boolean existsByReferralCode(String referralCode);

    Optional<Profile> findByReferralCode(String referralCode);
}
