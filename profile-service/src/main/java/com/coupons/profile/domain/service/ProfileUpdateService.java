package com.coupons.profile.domain.service;

import com.coupons.profile.domain.entity.Profile;
import com.coupons.profile.domain.exception.NothingToUpdateException;
import com.coupons.profile.domain.exception.ProfileNotFoundException;
import com.coupons.profile.infra.persistence.ProfileRepository;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProfileUpdateService {

    private final ProfileRepository profileRepository;

    public ProfileUpdateService(ProfileRepository profileRepository) {
        this.profileRepository = profileRepository;
    }

    @Transactional
    public Profile update(Profile patch) {
        UUID userId = patch.getUserId();
        Profile profile = profileRepository.findById(userId).orElseThrow(() -> new ProfileNotFoundException(userId));

        boolean hasDisplay = patch.getDisplayName() != null;
        boolean hasTimezone = patch.getTimezone() != null;
        if (!hasDisplay && !hasTimezone) {
            throw new NothingToUpdateException();
        }
        if (hasDisplay) {
            String name = patch.getDisplayName().trim();
            if (name.isEmpty()) {
                throw new NothingToUpdateException();
            }
            profile.setDisplayName(name);
        }
        if (hasTimezone) {
            profile.setTimezone(blankToNull(patch.getTimezone()));
        }
        return profileRepository.save(profile);
    }

    private static String blankToNull(String s) {
        if (s == null) {
            return null;
        }
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }
}
