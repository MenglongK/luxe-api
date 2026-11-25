package co.istad.corefeature.coupon.dto;

import co.istad.corefeature.common.enums.DiscountType;
import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record CouponValidationResponse(
        boolean isValid,
        String code,
        DiscountType discountType,
        BigDecimal discountValue,
        BigDecimal subtotal,
        BigDecimal discountAmount,
        BigDecimal totalAfterDiscount,
        String message
) {
}
