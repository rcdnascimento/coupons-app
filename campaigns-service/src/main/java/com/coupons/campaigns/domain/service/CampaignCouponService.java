package com.coupons.campaigns.domain.service;

import com.coupons.campaigns.domain.CouponStatus;
import com.coupons.campaigns.domain.entity.Campaign;
import com.coupons.campaigns.domain.entity.CampaignCoupon;
import com.coupons.campaigns.domain.entity.Coupon;
import com.coupons.campaigns.domain.exception.CampaignNotFoundException;
import com.coupons.campaigns.domain.exception.ConflictException;
import com.coupons.campaigns.infra.persistence.CampaignCouponRepository;
import com.coupons.campaigns.infra.persistence.CampaignRepository;
import com.coupons.campaigns.infra.persistence.CouponRepository;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CampaignCouponService {

    private final CampaignRepository campaignRepository;
    private final CouponRepository couponRepository;
    private final CampaignCouponRepository campaignCouponRepository;

    public CampaignCouponService(
            CampaignRepository campaignRepository,
            CouponRepository couponRepository,
            CampaignCouponRepository campaignCouponRepository) {
        this.campaignRepository = campaignRepository;
        this.couponRepository = couponRepository;
        this.campaignCouponRepository = campaignCouponRepository;
    }

    @Transactional
    public Campaign addCoupon(UUID campaignId, Coupon couponDraft, Integer linkPriority) {
        campaignRepository.findById(campaignId).orElseThrow(() -> new CampaignNotFoundException(campaignId));
        String code = couponDraft.getCode().trim();
        Coupon coupon =
                couponRepository
                        .findByCode(code)
                        .orElseGet(() -> createCoupon(code, couponDraft.getExpiresAt()));

        if (couponDraft.getTitle() != null && !couponDraft.getTitle().isBlank()) {
            coupon.setTitle(couponDraft.getTitle().trim());
            coupon = couponRepository.save(coupon);
        }

        if (campaignCouponRepository.existsByCampaignIdAndCouponId(campaignId, coupon.getId())) {
            throw new ConflictException("Cupom já vinculado à campanha");
        }

        CampaignCoupon link = new CampaignCoupon();
        link.setCampaignId(campaignId);
        link.setCouponId(coupon.getId());
        link.setPriority(linkPriority);
        campaignCouponRepository.save(link);

        coupon.setStatus(CouponStatus.ATTACHED_TO_CAMPAIGN);
        couponRepository.save(coupon);

        return campaignRepository.findById(campaignId).orElseThrow(() -> new CampaignNotFoundException(campaignId));
    }

    private Coupon createCoupon(String code, java.time.Instant expiresAt) {
        Coupon coupon = new Coupon();
        coupon.setCode(code);
        coupon.setExpiresAt(expiresAt);
        coupon.setStatus(CouponStatus.IN_INVENTORY);
        return couponRepository.save(coupon);
    }
}
