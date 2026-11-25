package co.istad.corefeature.wishlist.dto;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Builder
public record WishlistResponse(
        UUID id,
        String customerId,
        String name,
        List<WishlistItemResponse> items,
        Integer totalItems,
        Boolean isActive,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
