package com.coupons.prizes.infra.resource;

import com.coupons.prizes.domain.service.PrizeDeliveryQueryService;
import com.coupons.prizes.infra.resource.dto.PrizeDeliveryResponse;
import com.coupons.prizes.infra.resource.mapper.PrizeDeliveryRestMapper;
import java.util.List;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/prizes")
public class PrizesQueryResource {

    private final PrizeDeliveryQueryService prizeDeliveryQueryService;
    private final PrizeDeliveryRestMapper prizeDeliveryRestMapper;

    public PrizesQueryResource(
            PrizeDeliveryQueryService prizeDeliveryQueryService,
            PrizeDeliveryRestMapper prizeDeliveryRestMapper) {
        this.prizeDeliveryQueryService = prizeDeliveryQueryService;
        this.prizeDeliveryRestMapper = prizeDeliveryRestMapper;
    }

    @GetMapping("/users/{userId}")
    public List<PrizeDeliveryResponse> byUser(
            @PathVariable UUID userId, @RequestParam(required = false) UUID campaignId) {
        return prizeDeliveryRestMapper.toResponseList(
                prizeDeliveryQueryService.findByUser(userId, campaignId));
    }
}
