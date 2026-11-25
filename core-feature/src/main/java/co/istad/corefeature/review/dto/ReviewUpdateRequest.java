package co.istad.corefeature.review.dto;

import co.istad.corefeature.common.enums.FitFeedback;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record ReviewUpdateRequest(
        @Min(value = 1, message = "Rating must be at least 1")
        @Max(value = 5, message = "Rating cannot exceed 5")
        Integer rating,

        @Size(max = 150, message = "Review title cannot exceed 150 characters")
        String title,

        String comment,

        FitFeedback fitFeedback,

        String sizePurchased,

        String colorPurchased
) {
}
