package com.coupons.prizes.infra.resource.mapper;

import com.coupons.prizes.domain.entity.PrizeDelivery;
import com.coupons.prizes.infra.resource.dto.PrizeDeliveryResponse;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class PrizeDeliveryRestMapper {

    public PrizeDeliveryResponse toResponse(PrizeDelivery delivery) {
        return PrizeDeliveryResponse.from(delivery);
    }

    public List<PrizeDeliveryResponse> toResponseList(List<PrizeDelivery> deliveries) {
        return deliveries.stream().map(PrizeDeliveryResponse::from).collect(Collectors.toList());
    }
}
