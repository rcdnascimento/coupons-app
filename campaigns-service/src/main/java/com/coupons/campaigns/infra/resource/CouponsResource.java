package com.coupons.campaigns.infra.resource;

import com.coupons.campaigns.domain.CouponStatus;
import com.coupons.campaigns.domain.service.CouponCrudService;
import com.coupons.campaigns.infra.resource.dto.CouponResponse;
import com.coupons.campaigns.infra.resource.dto.CreateCouponRequest;
import com.coupons.campaigns.infra.resource.dto.UpdateCouponRequest;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/coupons")
public class CouponsResource {

    private final CouponCrudService couponCrudService;

    public CouponsResource(CouponCrudService couponCrudService) {
        this.couponCrudService = couponCrudService;
    }

    @GetMapping
    public List<CouponResponse> list() {
        return couponCrudService.listAll().stream()
                .map(CouponResponse::from)
                .collect(Collectors.toList());
    }

    @GetMapping("/search")
    public List<CouponResponse> search(
            @RequestParam(value = "q", required = false) String q,
            @RequestParam(value = "status", required = false) CouponStatus status) {
        return couponCrudService.searchByCodeOrTitle(q, 20, status).stream()
                .map(CouponResponse::from)
                .collect(Collectors.toList());
    }

    @GetMapping("/{couponId}")
    public CouponResponse get(@PathVariable UUID couponId) {
        return CouponResponse.from(couponCrudService.getById(couponId));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CouponResponse create(@Valid @RequestBody CreateCouponRequest request) {
        return CouponResponse.from(couponCrudService.create(request));
    }

    @PatchMapping("/{couponId}")
    public CouponResponse patch(@PathVariable UUID couponId, @Valid @RequestBody UpdateCouponRequest request) {
        return CouponResponse.from(couponCrudService.update(couponId, request));
    }
}
