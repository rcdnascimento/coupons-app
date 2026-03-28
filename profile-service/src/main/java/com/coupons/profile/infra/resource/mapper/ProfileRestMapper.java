package com.coupons.profile.infra.resource.mapper;

import com.coupons.profile.domain.entity.Profile;
import com.coupons.profile.infra.resource.dto.CreateProfileRequest;
import com.coupons.profile.infra.resource.dto.ProfileResponse;
import com.coupons.profile.infra.resource.dto.UpdateProfileRequest;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class ProfileRestMapper {

    public Profile toNewProfile(CreateProfileRequest request) {
        Profile profile = new Profile();
        profile.setUserId(request.getUserId());
        profile.setDisplayName(request.getDisplayName().trim());
        profile.setTimezone(blankToNull(request.getTimezone()));
        return profile;
    }

    public Profile toUpdatePatch(UUID userId, UpdateProfileRequest request) {
        Profile patch = new Profile();
        patch.setUserId(userId);
        patch.setDisplayName(request.getDisplayName());
        patch.setTimezone(request.getTimezone());
        return patch;
    }

    public ProfileResponse toResponse(Profile profile) {
        return ProfileResponse.from(profile);
    }

    private static String blankToNull(String s) {
        if (s == null) {
            return null;
        }
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }
}
