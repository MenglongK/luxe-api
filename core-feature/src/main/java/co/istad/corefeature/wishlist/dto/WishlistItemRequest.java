package co.istad.corefeature.wishlist.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record WishlistItemRequest(
        @NotNull(message = "Product variant ID is required")
        Long productVariantId,

        @Size(max = 255, message = "Note cannot exceed 255 characters")
        String note
) {
}
