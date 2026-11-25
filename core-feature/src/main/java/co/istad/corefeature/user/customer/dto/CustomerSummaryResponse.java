package co.istad.corefeature.user.customer.dto;

import co.istad.corefeature.common.enums.CustomerTier;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record CustomerSummaryResponse(
        UUID id,
        String customerCode,
        String name,
        String email,
        String phone,
        String avatarUrl,
        CustomerTier tier,
        Integer totalPoints,
        Long totalOrders,
        BigDecimal totalSpent,
        LocalDateTime lastOrderDate
) {
}
