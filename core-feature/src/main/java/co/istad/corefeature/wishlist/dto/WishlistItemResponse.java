package co.istad.corefeature.wishlist.dto;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
public record WishlistItemResponse(
        Long id,
        Long productVariantId,
        String productName,
        String skuCode,
        BigDecimal price,
        String note,
        LocalDateTime addedAt
) {
}
