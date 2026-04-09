package com.coupons.bff.resource;

import com.coupons.bff.infra.gateway.chest.DailyChestGateway;
import com.coupons.bff.infra.resource.dto.DailyChestTodayResponse;
import com.coupons.bff.infra.resource.dto.UserIdRequest;
import java.util.UUID;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@RequestMapping("/api/daily-chest")
public class DailyChestProxyResource {

    private final DailyChestGateway dailyChestGateway;

    public DailyChestProxyResource(DailyChestGateway dailyChestGateway) {
        this.dailyChestGateway = dailyChestGateway;
    }

    @PostMapping(value = "/open", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<DailyChestTodayResponse> open(@Valid @RequestBody UserIdRequest request) {
        return ResponseEntity.ok(dailyChestGateway.open(request.getUserId()));
    }

    @GetMapping(value = "/today", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<DailyChestTodayResponse> today(@RequestParam @NotNull UUID userId) {
        return ResponseEntity.ok(dailyChestGateway.today(userId));
    }
}
