package com.coupons.bff.resource;

import com.coupons.bff.domain.service.PrizeDeliveryEnrichmentService;
import com.coupons.bff.infra.gateway.prizes.PrizesGateway;
import com.coupons.bff.infra.resource.dto.PrizeDeliveryResponse;
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/prizes")
public class PrizesProxyResource {

    private final PrizesGateway prizesGateway;
    private final PrizeDeliveryEnrichmentService prizeDeliveryEnrichmentService;

    public PrizesProxyResource(
            PrizesGateway prizesGateway, PrizeDeliveryEnrichmentService prizeDeliveryEnrichmentService) {
        this.prizesGateway = prizesGateway;
        this.prizeDeliveryEnrichmentService = prizeDeliveryEnrichmentService;
    }

    @GetMapping(value = "/users/{userId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<PrizeDeliveryResponse>> byUser(
            @PathVariable String userId, @RequestParam(required = false) String campaignId) {
        List<PrizeDeliveryResponse> raw = prizesGateway.prizesByUser(userId, campaignId);
        return ResponseEntity.ok(prizeDeliveryEnrichmentService.enrich(raw));
    }
}
