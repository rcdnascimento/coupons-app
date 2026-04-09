package com.coupons.dailychest.infra.resource;

import com.coupons.dailychest.domain.entity.DailyChestOpening;
import com.coupons.dailychest.domain.service.DailyChestService;
import com.coupons.dailychest.infra.resource.dto.DailyChestTodayResponse;
import com.coupons.dailychest.infra.resource.dto.OpenChestRequest;
import java.util.Optional;
import java.util.UUID;
import javax.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/daily-chest")
public class DailyChestResource {

    private final DailyChestService dailyChestService;

    public DailyChestResource(DailyChestService dailyChestService) {
        this.dailyChestService = dailyChestService;
    }

    @PostMapping("/open")
    public DailyChestTodayResponse open(@Valid @RequestBody OpenChestRequest request) {
        DailyChestService.OpenResult result = dailyChestService.open(request.getUserId());
        DailyChestTodayResponse response = toResponse(result.getOpening());
        response.setAlreadyOpened(result.isAlreadyOpened());
        return response;
    }

    @GetMapping("/today")
    public DailyChestTodayResponse today(@RequestParam UUID userId) {
        Optional<DailyChestOpening> opening = dailyChestService.today(userId);
        if (opening.isEmpty()) {
            DailyChestTodayResponse response = new DailyChestTodayResponse();
            response.setOpenedToday(false);
            response.setRewardCoins(null);
            response.setOpenedAt(null);
            response.setLocalDate(dailyChestService.resolveTodayLocalDate(userId));
            response.setAlreadyOpened(null);
            return response;
        }
        return toResponse(opening.get());
    }

    private static DailyChestTodayResponse toResponse(DailyChestOpening opening) {
        DailyChestTodayResponse response = new DailyChestTodayResponse();
        response.setOpenedToday(true);
        response.setRewardCoins(opening.getRewardCoins());
        response.setLocalDate(opening.getLocalDate());
        response.setOpenedAt(opening.getCreatedAt());
        return response;
    }
}
