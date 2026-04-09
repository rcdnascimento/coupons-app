package com.coupons.bff.domain.service;

import com.coupons.bff.infra.gateway.campaigns.CampaignsGateway;
import com.coupons.bff.infra.gateway.coupons.CouponsGateway;
import com.coupons.bff.infra.resource.dto.CampaignResponse;
import com.coupons.bff.infra.resource.dto.CouponResponse;
import com.coupons.bff.infra.resource.dto.PrizeDeliveryResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class PrizeDeliveryEnrichmentService {

    private final CampaignsGateway campaignsGateway;
    private final CouponsGateway couponsGateway;

    public PrizeDeliveryEnrichmentService(CampaignsGateway campaignsGateway, CouponsGateway couponsGateway) {
        this.campaignsGateway = campaignsGateway;
        this.couponsGateway = couponsGateway;
    }

    public List<PrizeDeliveryResponse> enrich(List<PrizeDeliveryResponse> prizes) {
        if (prizes == null || prizes.isEmpty()) {
            return prizes;
        }
        Set<UUID> campaignIds =
                prizes.stream().map(PrizeDeliveryResponse::getCampaignId).filter(Objects::nonNull).collect(Collectors.toSet());
        Set<UUID> couponIds =
                prizes.stream().map(PrizeDeliveryResponse::getCouponId).filter(Objects::nonNull).collect(Collectors.toSet());

        Map<UUID, CampaignResponse> campaignById = new HashMap<>();
        for (UUID id : campaignIds) {
            try {
                campaignById.put(id, campaignsGateway.getCampaign(id.toString()));
            } catch (RuntimeException ignored) {
                // campanha indisponível: campos enriquecidos ficam vazios
            }
        }

        Map<UUID, CouponResponse> couponById = new HashMap<>();
        for (UUID id : couponIds) {
            try {
                couponById.put(id, couponsGateway.getCoupon(id.toString()));
            } catch (RuntimeException ignored) {
                // cupom indisponível
            }
        }

        for (PrizeDeliveryResponse p : prizes) {
            CampaignResponse c = p.getCampaignId() != null ? campaignById.get(p.getCampaignId()) : null;
            if (c != null) {
                p.setCampaignTitle(c.getTitle());
                p.setCompanyName(c.getCompanyName());
                p.setCompanyLogoUrl(c.getCompanyLogoUrl());
            }
            CouponResponse coupon = p.getCouponId() != null ? couponById.get(p.getCouponId()) : null;
            if (coupon != null && coupon.getTitle() != null && !coupon.getTitle().isBlank()) {
                p.setCouponTitle(coupon.getTitle().trim());
            }
        }
        return prizes;
    }
}
