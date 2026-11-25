package co.istad.corefeature.product.product_variant.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Builder;

import java.math.BigDecimal;
import java.util.Map;

@Builder
public record ProductVariantRequest(
        @NotNull(message = "Product ID is required")
        @Positive(message = "Product ID must be a positive number")
        Long productId,

        Map<String, String> attributes,

        @NotNull(message = "Price is required")
        @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than zero")
        BigDecimal price,

        @DecimalMin(value = "0.0", message = "Price in must be non-negative")
        BigDecimal priceIn,

        Boolean isActive
) {
}
