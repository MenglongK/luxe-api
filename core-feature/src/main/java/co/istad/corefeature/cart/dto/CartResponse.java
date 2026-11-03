package co.istad.corefeature.cart.dto;

import lombok.Builder;

import java.util.List;
import java.util.UUID;

@Builder
public record CartResponse(
        UUID id,
        String customerId,
        List<CartItemResponse> items,
        Boolean isActive
) {
}
