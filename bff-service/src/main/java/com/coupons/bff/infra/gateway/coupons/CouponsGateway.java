package com.coupons.bff.infra.gateway.coupons;

import com.coupons.bff.infra.resource.dto.CouponResponse;
import com.coupons.bff.infra.resource.dto.CreateCouponRequest;
import com.coupons.bff.infra.resource.dto.UpdateCouponRequest;
import java.util.List;

public interface CouponsGateway {

    List<CouponResponse> listCoupons();

    List<CouponResponse> searchCoupons(String q, String status);

    CouponResponse getCoupon(String couponId);

    CouponResponse createCoupon(CreateCouponRequest request);

    CouponResponse patchCoupon(String couponId, UpdateCouponRequest request);
}
