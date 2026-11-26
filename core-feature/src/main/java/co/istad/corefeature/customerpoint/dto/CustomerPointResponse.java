package co.istad.corefeature.customerpoint.dto;

import co.istad.corefeature.common.enums.CustomerTier;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record CustomerPointResponse(
        Long id,
        String membershipNumber,
        String customerId,
        Integer totalPoints,
        Integer lifetimePoints,
        CustomerTier tier,
        CustomerTier nextTier,
        Integer pointsToNextTier,
        Boolean isActive,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
