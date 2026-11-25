package co.istad.corefeature.product.product_variant.dto;

import lombok.Builder;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

@Builder
public record ProductVariantResponse(
        Long id,
        UUID skuId,
        Long productId,
        String productName,
        Map<String, String> attributes,
        BigDecimal price,
        BigDecimal priceIn,
        Boolean isActive
) {
}
