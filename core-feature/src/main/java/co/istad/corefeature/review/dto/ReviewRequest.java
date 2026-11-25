package co.istad.corefeature.review.dto;

import co.istad.corefeature.common.enums.FitFeedback;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record ReviewRequest(
        @NotNull(message = "Product ID is required")
        Long productId,

        @NotBlank(message = "Customer ID is required")
        String customerId,

        @NotBlank(message = "Author name is required")
        String authorName,

        @NotNull(message = "Rating is required")
        @Min(value = 1, message = "Rating must be at least 1")
        @Max(value = 5, message = "Rating cannot exceed 5")
        Integer rating,

        @NotBlank(message = "Review title is required")
        @Size(max = 150, message = "Review title cannot exceed 150 characters")
        String title,

        @NotBlank(message = "Review comment is required")
        String comment,

        FitFeedback fitFeedback,

        String sizePurchased,

        String colorPurchased
) {
}
