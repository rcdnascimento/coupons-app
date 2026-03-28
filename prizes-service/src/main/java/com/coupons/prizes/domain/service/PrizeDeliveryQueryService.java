package com.coupons.prizes.domain.service;

import com.coupons.prizes.domain.entity.PrizeDelivery;
import com.coupons.prizes.infra.persistence.PrizeDeliveryRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PrizeDeliveryQueryService {

    private final PrizeDeliveryRepository prizeDeliveryRepository;

    public PrizeDeliveryQueryService(PrizeDeliveryRepository prizeDeliveryRepository) {
        this.prizeDeliveryRepository = prizeDeliveryRepository;
    }

    @Transactional(readOnly = true)
    public List<PrizeDelivery> findByUser(UUID userId, UUID campaignId) {
        if (campaignId == null) {
            return prizeDeliveryRepository.findByUserIdOrderByProcessedAtDesc(userId);
        }
        return prizeDeliveryRepository.findByUserIdAndCampaignIdOrderByProcessedAtDesc(userId, campaignId);
    }
}
