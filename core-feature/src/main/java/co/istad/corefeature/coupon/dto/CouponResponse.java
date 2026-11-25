package co.istad.corefeature.coupon.dto;

import co.istad.corefeature.common.enums.CouponStatus;
import co.istad.corefeature.common.enums.DiscountType;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record CouponResponse(
        UUID id,
        String code,
        String description,
        DiscountType discountType,
        BigDecimal discountValue,
        BigDecimal minSpend,
        BigDecimal maxDiscount,
        Integer usageCount,
        Integer usageLimit,
        LocalDate startDate,
        LocalDate expiryDate,
        CouponStatus status,
        Boolean isCurrentlyUsable,
        Boolean isActive,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
