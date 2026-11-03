package co.istad.corefeature.brand.dto;

import lombok.Builder;

@Builder
public record BrandResponse(
        Long id,
        String name,
        String logoUrl,
        Boolean isActive
) {
}
