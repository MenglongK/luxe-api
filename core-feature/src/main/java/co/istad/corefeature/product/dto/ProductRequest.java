package co.istad.corefeature.product.dto;

import co.istad.corefeature.common.enums.DiscountType;
import co.istad.corefeature.common.enums.Gender;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Builder;

import java.util.List;

@Builder
public record ProductRequest(
        @NotBlank(message = "Product name is required and cannot be empty")
        @Size(min = 2, max = 255, message = "Product name must be between 2 and 255 characters")
        String name,

        @Size(max = 2000, message = "Description must not exceed 2000 characters")
        String descriptions,

        @NotNull(message = "Gender is required (MEN, WOMEN, CHILDREN)")
        Gender gender,

        DiscountType discountType,

        Boolean isActive,

        @Size(max = 50, message = "SKU code must not exceed 50 characters")
        String skuCode,

        @Size(max = 255, message = "Thumbnail URL must not exceed 255 characters")
        String thumbnailUrl,

        List<String> imageUrls,

        @NotNull(message = "Brand ID is required")
        @Positive(message = "Brand ID must be a positive number")
        Long brandId,

        @NotNull(message = "Category ID is required")
        @Positive(message = "Category ID must be a positive number")
        Long categoryId,

        @Positive(message = "Currency ID must be a positive number")
        Long currencyId,

        @Positive(message = "Supplier ID must be a positive number")
        Long supplierId
) {
}
