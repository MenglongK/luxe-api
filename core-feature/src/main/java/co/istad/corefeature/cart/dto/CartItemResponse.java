package co.istad.corefeature.cart.dto;

import lombok.Builder;

@Builder
public record CartItemResponse(
        Long id,
        Long productVariantId,
        Integer quantity
) {
}
