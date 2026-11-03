package co.istad.corefeature.customerpoint.dto;

import lombok.Builder;

@Builder
public record CustomerPointResponse(
        Long id,
        String customerId,
        Integer totalPoints,
        Boolean isActive
) {
}
