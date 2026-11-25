package co.istad.corefeature.review.dto;

import co.istad.corefeature.common.enums.FitFeedback;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record ReviewResponse(
        Long id,
        Long productId,
        String productName,
        String customerId,
        String authorName,
        Integer rating,
        String title,
        String comment,
        Boolean isVerifiedBuyer,
        FitFeedback fitFeedback,
        String sizePurchased,
        String colorPurchased,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
