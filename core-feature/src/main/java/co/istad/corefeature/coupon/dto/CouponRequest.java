package co.istad.corefeature.coupon.dto;

import co.istad.corefeature.common.enums.CouponStatus;
import co.istad.corefeature.common.enums.DiscountType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;

@Builder
public record CouponRequest(
        @NotBlank(message = "Coupon code is required")
        @Size(max = 50, message = "Coupon code cannot exceed 50 characters")
        String code,

        @NotBlank(message = "Description is required")
        @Size(max = 255, message = "Description cannot exceed 255 characters")
        String description,

        @NotNull(message = "Discount type is required (PERCENTAGE or FIXED_AMOUNT)")
        DiscountType discountType,

        @NotNull(message = "Discount value is required")
        @Positive(message = "Discount value must be greater than zero")
        BigDecimal discountValue,

        @PositiveOrZero(message = "Minimum spend must be zero or positive")
        BigDecimal minSpend,

        @PositiveOrZero(message = "Maximum discount must be zero or positive")
        BigDecimal maxDiscount,

        @Positive(message = "Usage limit must be positive")
        Integer usageLimit,

        LocalDate startDate,

        LocalDate expiryDate,

        CouponStatus status
) {
}
