package com.coupons.profile.resource;

import com.coupons.profile.domain.service.ProfileCreationService;
import com.coupons.profile.domain.service.ProfileQueryService;
import com.coupons.profile.domain.service.ProfileUpdateService;
import com.coupons.profile.infra.resource.dto.CreateProfileRequest;
import com.coupons.profile.infra.resource.dto.ProfileResponse;
import com.coupons.profile.infra.resource.dto.UpdateProfileRequest;
import com.coupons.profile.infra.resource.mapper.ProfileRestMapper;
import java.util.UUID;
import javax.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/profiles")
public class ProfileResource {

    private final ProfileCreationService profileCreationService;
    private final ProfileQueryService profileQueryService;
    private final ProfileUpdateService profileUpdateService;
    private final ProfileRestMapper profileRestMapper;

    public ProfileResource(
            ProfileCreationService profileCreationService,
            ProfileQueryService profileQueryService,
            ProfileUpdateService profileUpdateService,
            ProfileRestMapper profileRestMapper) {
        this.profileCreationService = profileCreationService;
        this.profileQueryService = profileQueryService;
        this.profileUpdateService = profileUpdateService;
        this.profileRestMapper = profileRestMapper;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProfileResponse create(@Valid @RequestBody CreateProfileRequest request) {
        return profileRestMapper.toResponse(
                profileCreationService.create(
                        profileRestMapper.toNewProfile(request), request.getReferralCode()));
    }

    @GetMapping("/{userId}")
    public ProfileResponse get(@PathVariable UUID userId) {
        return profileRestMapper.toResponse(profileQueryService.getByUserId(userId));
    }

    @PutMapping("/{userId}")
    public ProfileResponse update(@PathVariable UUID userId, @Valid @RequestBody UpdateProfileRequest request) {
        return profileRestMapper.toResponse(
                profileUpdateService.update(profileRestMapper.toUpdatePatch(userId, request)));
    }
}
