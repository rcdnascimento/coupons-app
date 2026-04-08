package com.coupons.campaigns.domain.service;

import com.coupons.campaigns.domain.entity.CampaignAllocation;
import com.coupons.campaigns.domain.entity.CampaignCoupon;
import com.coupons.campaigns.domain.entity.Coupon;
import com.coupons.campaigns.domain.exception.CampaignNotFoundException;
import com.coupons.campaigns.infra.persistence.CampaignAllocationRepository;
import com.coupons.campaigns.infra.persistence.CampaignCouponRepository;
import com.coupons.campaigns.infra.persistence.CampaignRepository;
import com.coupons.campaigns.infra.persistence.CouponRepository;
import com.coupons.campaigns.infra.resource.dto.CampaignWinnersResponse;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Vencedores = alocações da campanha, ordenadas para ranking público:
 * menor {@code priority} do vínculo campanha–cupom primeiro (1º prémio antes do 2º); empatados por
 * {@code allocatedAt} crescente; empate final por {@code userId} para ordem estável.
 */
@Service
public class CampaignWinnersService {

    private static final Logger log = LoggerFactory.getLogger(CampaignWinnersService.class);

    private final CampaignRepository campaignRepository;
    private final CampaignAllocationRepository campaignAllocationRepository;
    private final CampaignCouponRepository campaignCouponRepository;
    private final CouponRepository couponRepository;

    public CampaignWinnersService(
            CampaignRepository campaignRepository,
            CampaignAllocationRepository campaignAllocationRepository,
            CampaignCouponRepository campaignCouponRepository,
            CouponRepository couponRepository) {
        this.campaignRepository = campaignRepository;
        this.campaignAllocationRepository = campaignAllocationRepository;
        this.campaignCouponRepository = campaignCouponRepository;
        this.couponRepository = couponRepository;
    }

    @Transactional(readOnly = true)
    public CampaignWinnersResponse winners(UUID campaignId) {
        if (!campaignRepository.existsById(campaignId)) {
            throw new CampaignNotFoundException(campaignId);
        }

        List<CampaignAllocation> allocations = campaignAllocationRepository.findByCampaignId(campaignId);
        List<CampaignCoupon> links = campaignCouponRepository.findByCampaignIdOrderByPriorityAsc(campaignId);
        Map<UUID, Integer> priorityByCouponId = new HashMap<>();
        for (CampaignCoupon link : links) {
            priorityByCouponId.put(link.getCouponId(), link.getPriority());
        }

        List<AllocationRow> rows = new ArrayList<>();
        for (CampaignAllocation allocation : allocations) {
            Coupon coupon = couponRepository.findById(allocation.getCouponId()).orElse(null);
            String title = coupon != null ? displayTitle(coupon) : "Cupom";
            Integer linkPriority = priorityByCouponId.get(allocation.getCouponId());
            int sortPriority = linkPriority != null ? linkPriority : Integer.MAX_VALUE;
            rows.add(new AllocationRow(allocation, title, sortPriority, linkPriority));
        }

        rows.sort(
                Comparator.comparingInt((AllocationRow r) -> r.sortPriority)
                        .thenComparing(r -> r.allocation.getAllocatedAt(), Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(r -> r.allocation.getUserId(), Comparator.nullsLast(Comparator.naturalOrder())));

        List<CampaignWinnersResponse.WinnerEntry> entries = new ArrayList<>();
        int rank = 1;
        for (AllocationRow row : rows) {
            CampaignWinnersResponse.WinnerEntry entry = new CampaignWinnersResponse.WinnerEntry();
            entry.setRank(rank++);
            entry.setCouponTitle(row.couponTitle);
            entry.setPriority(row.displayPriority);
            entry.setUserId(row.allocation.getUserId());
            entries.add(entry);
        }

        CampaignWinnersResponse response = new CampaignWinnersResponse();
        response.setEntries(entries);
        log.debug("Ranking de vencedores campaignId={}: {} entrada(s).", campaignId, entries.size());
        return response;
    }

    private static String displayTitle(Coupon c) {
        if (c.getTitle() != null && !c.getTitle().isBlank()) {
            return c.getTitle().trim();
        }
        return "Cupom";
    }

    private static final class AllocationRow {
        private final CampaignAllocation allocation;
        private final String couponTitle;
        private final int sortPriority;
        private final Integer displayPriority;

        private AllocationRow(
                CampaignAllocation allocation,
                String couponTitle,
                int sortPriority,
                Integer displayPriority) {
            this.allocation = allocation;
            this.couponTitle = couponTitle;
            this.sortPriority = sortPriority;
            this.displayPriority = displayPriority;
        }
    }
}
