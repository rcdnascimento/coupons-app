package com.coupons.bff.resource;

import com.coupons.bff.infra.gateway.chest.DailyChestGateway;
import com.coupons.bff.infra.resource.dto.DailyChestTodayResponse;
import com.coupons.bff.security.SecurityRequestSupport;
import java.util.UUID;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/daily-chest")
public class DailyChestProxyResource {

    private final DailyChestGateway dailyChestGateway;

    public DailyChestProxyResource(DailyChestGateway dailyChestGateway) {
        this.dailyChestGateway = dailyChestGateway;
    }

    @PostMapping(value = "/open", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<DailyChestTodayResponse> open() {
        UUID userId = SecurityRequestSupport.requireUserId();
        return ResponseEntity.ok(dailyChestGateway.open(userId));
    }

    @GetMapping(value = "/today", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<DailyChestTodayResponse> today() {
        UUID userId = SecurityRequestSupport.requireUserId();
        return ResponseEntity.ok(dailyChestGateway.today(userId));
    }
}
