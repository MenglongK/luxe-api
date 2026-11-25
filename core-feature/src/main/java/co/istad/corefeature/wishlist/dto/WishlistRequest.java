package co.istad.corefeature.wishlist.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record WishlistRequest(
        @NotBlank(message = "Customer ID is required")
        String customerId,

        @Size(max = 100, message = "Wishlist name cannot exceed 100 characters")
        String name
) {
}
