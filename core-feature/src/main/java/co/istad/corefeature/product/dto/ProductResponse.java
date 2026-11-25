package co.istad.corefeature.product.dto;

import co.istad.corefeature.common.enums.DiscountType;
import co.istad.corefeature.common.enums.Gender;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Builder
public record ProductResponse(
        Long id,
        UUID productId,
        String name,
        String descriptions,
        Gender gender,
        DiscountType discountType,
        Boolean isActive,
        String skuCode,
        String qrCodeHash,
        String thumbnailUrl,
        List<String> imageUrls,
        Long brandId,
        Long categoryId,
        Long currencyId,
        Long supplierId,
        LocalDateTime createdAt,
        String createdBy,
        LocalDateTime updatedAt,
        String updatedBy
) {
}
