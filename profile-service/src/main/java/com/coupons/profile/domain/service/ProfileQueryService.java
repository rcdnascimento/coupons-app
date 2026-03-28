package com.coupons.profile.domain.service;

import com.coupons.profile.domain.entity.Profile;
import com.coupons.profile.domain.exception.ProfileNotFoundException;
import com.coupons.profile.infra.persistence.ProfileRepository;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProfileQueryService {

    private final ProfileRepository profileRepository;

    public ProfileQueryService(ProfileRepository profileRepository) {
        this.profileRepository = profileRepository;
    }

    @Transactional(readOnly = true)
    public Profile getByUserId(UUID userId) {
        return profileRepository.findById(userId).orElseThrow(() -> new ProfileNotFoundException(userId));
    }
}
