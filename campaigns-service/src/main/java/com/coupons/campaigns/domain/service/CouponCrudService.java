package com.coupons.campaigns.domain.service;

import com.coupons.campaigns.domain.CouponStatus;
import com.coupons.campaigns.domain.entity.Coupon;
import com.coupons.campaigns.domain.exception.ConflictException;
import com.coupons.campaigns.domain.exception.CouponNotFoundException;
import com.coupons.campaigns.infra.persistence.CampaignAllocationRepository;
import com.coupons.campaigns.infra.persistence.CampaignCouponRepository;
import com.coupons.campaigns.infra.persistence.CouponRepository;
import com.coupons.campaigns.infra.resource.dto.CreateCouponRequest;
import com.coupons.campaigns.infra.resource.dto.UpdateCouponRequest;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CouponCrudService {

    private final CouponRepository couponRepository;
    private final CampaignCouponRepository campaignCouponRepository;
    private final CampaignAllocationRepository campaignAllocationRepository;

    public CouponCrudService(
            CouponRepository couponRepository,
            CampaignCouponRepository campaignCouponRepository,
            CampaignAllocationRepository campaignAllocationRepository) {
        this.couponRepository = couponRepository;
        this.campaignCouponRepository = campaignCouponRepository;
        this.campaignAllocationRepository = campaignAllocationRepository;
    }

    @Transactional(readOnly = true)
    public List<Coupon> listAll() {
        return couponRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
    }

    @Transactional(readOnly = true)
    public List<Coupon> searchByCodeOrTitle(String rawQuery, int limit, CouponStatus statusFilter) {
        String q = rawQuery == null ? "" : rawQuery.trim();
        if (q.isEmpty()) {
            return List.of();
        }
        int cap = Math.min(Math.max(limit, 1), 50);
        var page = PageRequest.of(0, cap);
        if (statusFilter != null) {
            return couponRepository.searchByCodeOrTitleAndStatus(q, statusFilter, page);
        }
        return couponRepository.searchByCodeOrTitle(q, page);
    }

    @Transactional(readOnly = true)
    public Coupon getById(UUID id) {
        return couponRepository.findById(id).orElseThrow(() -> new CouponNotFoundException(id));
    }

    @Transactional
    public Coupon create(CreateCouponRequest request) {
        String code = request.getCode().trim();
        if (couponRepository.existsByCode(code)) {
            throw new ConflictException("Já existe cupom com este código");
        }
        Coupon coupon = new Coupon();
        coupon.setCode(code);
        coupon.setExpiresAt(request.getExpiresAt());
        coupon.setTitle(request.getTitle().trim());
        coupon.setStatus(CouponStatus.IN_INVENTORY);
        return couponRepository.save(coupon);
    }

    @Transactional
    public void delete(UUID id) {
        Coupon coupon = getById(id);
        if (campaignCouponRepository.findByCouponId(id).isPresent()) {
            throw new ConflictException("Cupom associado a uma campanha; remova a associação antes de apagar.");
        }
        if (campaignAllocationRepository.existsByCouponId(id)) {
            throw new ConflictException("Não é possível apagar cupom já alocado.");
        }
        couponRepository.delete(coupon);
    }

    @Transactional
    public Coupon update(UUID id, UpdateCouponRequest request) {
        Coupon coupon = getById(id);
        if (request.getTitle() != null) {
            coupon.setTitle(request.getTitle().isBlank() ? null : request.getTitle().trim());
        }
        if (request.getExpiresAt() != null) {
            coupon.setExpiresAt(request.getExpiresAt());
        }
        return couponRepository.save(coupon);
    }
}
