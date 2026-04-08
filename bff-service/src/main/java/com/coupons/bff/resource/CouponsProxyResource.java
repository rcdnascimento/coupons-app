package com.coupons.bff.resource;

import com.coupons.bff.infra.gateway.coupons.CouponsGateway;
import com.coupons.bff.infra.resource.dto.CouponResponse;
import com.coupons.bff.infra.resource.dto.CreateCouponRequest;
import com.coupons.bff.infra.resource.dto.UpdateCouponRequest;
import java.util.List;
import javax.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/coupons")
public class CouponsProxyResource {

    private final CouponsGateway couponsGateway;

    public CouponsProxyResource(CouponsGateway couponsGateway) {
        this.couponsGateway = couponsGateway;
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<CouponResponse>> list() {
        return ResponseEntity.ok(couponsGateway.listCoupons());
    }

    @GetMapping(value = "/search", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<CouponResponse>> search(
            @RequestParam(value = "q", required = false) String q,
            @RequestParam(value = "status", required = false) String status) {
        return ResponseEntity.ok(couponsGateway.searchCoupons(q, status));
    }

    @GetMapping(value = "/{couponId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CouponResponse> getOne(@PathVariable String couponId) {
        return ResponseEntity.ok(couponsGateway.getCoupon(couponId));
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CouponResponse> create(@Valid @RequestBody CreateCouponRequest body) {
        return ResponseEntity.status(HttpStatus.CREATED).body(couponsGateway.createCoupon(body));
    }

    @PatchMapping(
            value = "/{couponId}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CouponResponse> patch(
            @PathVariable String couponId, @Valid @RequestBody UpdateCouponRequest body) {
        return ResponseEntity.ok(couponsGateway.patchCoupon(couponId, body));
    }
}
