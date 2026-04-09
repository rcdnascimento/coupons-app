package com.coupons.campaigns.domain.service;

import com.coupons.campaigns.domain.CouponStatus;
import com.coupons.campaigns.domain.entity.Campaign;
import com.coupons.campaigns.domain.entity.CampaignCoupon;
import com.coupons.campaigns.domain.entity.Coupon;
import com.coupons.campaigns.domain.exception.CampaignNotFoundException;
import com.coupons.campaigns.domain.exception.ConflictException;
import com.coupons.campaigns.domain.exception.CouponNotFoundException;
import com.coupons.campaigns.infra.persistence.CampaignAllocationRepository;
import com.coupons.campaigns.infra.persistence.CampaignCouponRepository;
import com.coupons.campaigns.infra.persistence.CampaignRepository;
import com.coupons.campaigns.infra.persistence.CouponRepository;
import com.coupons.campaigns.infra.resource.dto.CampaignCouponLinkResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CampaignCouponService {

    private final CampaignRepository campaignRepository;
    private final CouponRepository couponRepository;
    private final CampaignCouponRepository campaignCouponRepository;
    private final CampaignAllocationRepository campaignAllocationRepository;

    public CampaignCouponService(
            CampaignRepository campaignRepository,
            CouponRepository couponRepository,
            CampaignCouponRepository campaignCouponRepository,
            CampaignAllocationRepository campaignAllocationRepository) {
        this.campaignRepository = campaignRepository;
        this.couponRepository = couponRepository;
        this.campaignCouponRepository = campaignCouponRepository;
        this.campaignAllocationRepository = campaignAllocationRepository;
    }

    @Transactional(readOnly = true)
    public List<CampaignCouponLinkResponse> listLinkedCoupons(UUID campaignId) {
        campaignRepository.findById(campaignId).orElseThrow(() -> new CampaignNotFoundException(campaignId));
        List<CampaignCouponLinkResponse> out = new ArrayList<>();
        for (CampaignCoupon link : campaignCouponRepository.findByCampaignIdOrderByPriorityAsc(campaignId)) {
            Coupon c = couponRepository.findById(link.getCouponId()).orElse(null);
            if (c == null) {
                continue;
            }
            CampaignCouponLinkResponse row = new CampaignCouponLinkResponse();
            row.setCouponId(c.getId());
            row.setCode(c.getCode());
            row.setTitle(c.getTitle());
            row.setPriority(link.getPriority());
            row.setAllocated(campaignAllocationRepository.existsByCouponId(link.getCouponId()));
            out.add(row);
        }
        return out;
    }

    @Transactional
    public void removeCouponFromCampaign(UUID campaignId, UUID couponId) {
        campaignRepository.findById(campaignId).orElseThrow(() -> new CampaignNotFoundException(campaignId));
        CampaignCoupon link =
                campaignCouponRepository
                        .findByCampaignIdAndCouponId(campaignId, couponId)
                        .orElseThrow(() -> new ConflictException("Cupom não está associado a esta campanha"));
        if (campaignAllocationRepository.existsByCouponId(couponId)) {
            throw new ConflictException("Cupom já foi alocado; não é possível remover a associação.");
        }
        campaignCouponRepository.delete(link);
        Coupon coupon = couponRepository.findById(couponId).orElseThrow(() -> new CouponNotFoundException(couponId));
        coupon.setStatus(CouponStatus.IN_INVENTORY);
        couponRepository.save(coupon);
    }

    @Transactional
    public Campaign addCoupon(UUID campaignId, Coupon couponDraft, Integer linkPriority) {
        campaignRepository.findById(campaignId).orElseThrow(() -> new CampaignNotFoundException(campaignId));
        String code = couponDraft.getCode().trim();
        Coupon coupon =
                couponRepository
                        .findByCode(code)
                        .orElseThrow(
                                () ->
                                        new CouponNotFoundException(
                                                "Cupom não encontrado no inventário para o código indicado; crie o cupom antes de associar."));

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
}
