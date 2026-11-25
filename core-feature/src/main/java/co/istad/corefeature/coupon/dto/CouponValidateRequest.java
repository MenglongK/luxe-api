package co.istad.corefeature.coupon.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record CouponValidateRequest(
        @NotBlank(message = "Coupon code is required")
        String code,

        @NotNull(message = "Cart subtotal is required")
        @PositiveOrZero(message = "Cart subtotal must be zero or positive")
        BigDecimal subtotal
) {
}
