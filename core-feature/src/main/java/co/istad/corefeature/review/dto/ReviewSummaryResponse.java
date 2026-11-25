package co.istad.corefeature.review.dto;

import lombok.Builder;

import java.util.Map;

@Builder
public record ReviewSummaryResponse(
        Long productId,
        Double averageRating,
        Long totalReviews,
        Map<Integer, Long> ratingDistribution,
        Map<String, Long> fitFeedbackBreakdown
) {
}
