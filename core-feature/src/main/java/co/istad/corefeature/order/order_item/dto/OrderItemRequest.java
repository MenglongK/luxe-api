package co.istad.corefeature.order.order_item.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record OrderItemRequest(
        @NotNull(message = "Product variant ID is required")
        Long productVariantId,

        @NotNull(message = "Quantity is required")
        @Min(value = 1, message = "Quantity must be at least 1")
        Integer quantity
) {
}
