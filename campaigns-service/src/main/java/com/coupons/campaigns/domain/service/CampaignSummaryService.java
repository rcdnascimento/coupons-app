package com.coupons.campaigns.domain.service;

import com.coupons.campaigns.domain.entity.CampaignCoupon;
import com.coupons.campaigns.domain.entity.Coupon;
import com.coupons.campaigns.domain.exception.CampaignNotFoundException;
import com.coupons.campaigns.infra.persistence.CampaignCouponRepository;
import com.coupons.campaigns.infra.persistence.CampaignRepository;
import com.coupons.campaigns.infra.persistence.CouponRepository;
import com.coupons.campaigns.infra.resource.dto.CampaignSummaryResponse;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CampaignSummaryService {

    private final CampaignRepository campaignRepository;
    private final CampaignCouponRepository campaignCouponRepository;
    private final CouponRepository couponRepository;

    public CampaignSummaryService(
            CampaignRepository campaignRepository,
            CampaignCouponRepository campaignCouponRepository,
            CouponRepository couponRepository) {
        this.campaignRepository = campaignRepository;
        this.campaignCouponRepository = campaignCouponRepository;
        this.couponRepository = couponRepository;
    }

    @Transactional(readOnly = true)
    public CampaignSummaryResponse summary(UUID campaignId) {
        if (!campaignRepository.existsById(campaignId)) {
            throw new CampaignNotFoundException(campaignId);
        }

        List<CampaignCoupon> links = campaignCouponRepository.findByCampaignIdOrderByPriorityAsc(campaignId);

        Map<String, Long> quantityByTitle = new LinkedHashMap<>();
        for (CampaignCoupon link : links) {
            Coupon c = couponRepository.findById(link.getCouponId()).orElse(null);
            if (c == null) {
                continue;
            }
            String title = displayTitle(c);
            quantityByTitle.merge(title, 1L, Long::sum);
        }

        List<CampaignSummaryResponse.PossiblePrizeResponse> possiblePrizes = new ArrayList<>();
        for (Map.Entry<String, Long> e : quantityByTitle.entrySet()) {
            CampaignSummaryResponse.PossiblePrizeResponse item =
                    new CampaignSummaryResponse.PossiblePrizeResponse();
            item.setTitle(e.getKey());
            item.setQuantity(e.getValue());
            possiblePrizes.add(item);
        }

        CampaignSummaryResponse response = new CampaignSummaryResponse();
        response.setCampaignId(campaignId);
        response.setPossiblePrizes(possiblePrizes);
        return response;
    }

    private static String displayTitle(Coupon c) {
        if (c.getTitle() != null && !c.getTitle().isBlank()) {
            return c.getTitle().trim();
        }
        return "Cupom";
    }
}
