package co.istad.corefeature.order.order_item.dto;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record OrderItemResponse(
        Long id,
        Long productVariantId,
        String productName,
        String skuCode,
        Integer quantity,
        BigDecimal unitPrice,
        BigDecimal totalPrice
) {
}
