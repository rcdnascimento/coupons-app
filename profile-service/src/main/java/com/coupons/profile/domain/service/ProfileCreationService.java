package com.coupons.profile.domain.service;

import com.coupons.profile.domain.entity.Profile;
import com.coupons.profile.domain.exception.ProfileAlreadyExistsException;
import com.coupons.profile.infra.persistence.ProfileRepository;
import java.security.SecureRandom;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProfileCreationService {

    private static final String REFERRAL_ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int REFERRAL_CODE_LENGTH = 8;
    private static final int MAX_REFERRAL_GENERATION_ATTEMPTS = 20;

    private final ProfileRepository profileRepository;
    private final SecureRandom secureRandom = new SecureRandom();

    public ProfileCreationService(ProfileRepository profileRepository) {
        this.profileRepository = profileRepository;
    }

    @Transactional
    public Profile create(Profile draft) {
        UUID userId = draft.getUserId();
        if (profileRepository.existsById(userId)) {
            throw new ProfileAlreadyExistsException(userId);
        }
        draft.setReferralCode(generateUniqueReferralCode());
        profileRepository.save(draft);
        return draft;
    }

    private String generateUniqueReferralCode() {
        for (int i = 0; i < MAX_REFERRAL_GENERATION_ATTEMPTS; i++) {
            String code = randomReferralCode();
            if (!profileRepository.existsByReferralCode(code)) {
                return code;
            }
        }
        throw new IllegalStateException("Não foi possível gerar código de indicação único");
    }

    private String randomReferralCode() {
        StringBuilder sb = new StringBuilder(REFERRAL_CODE_LENGTH);
        for (int i = 0; i < REFERRAL_CODE_LENGTH; i++) {
            sb.append(REFERRAL_ALPHABET.charAt(secureRandom.nextInt(REFERRAL_ALPHABET.length())));
        }
        return sb.toString();
    }
}
