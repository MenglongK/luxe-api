package co.istad.corefeature.review.service;

import co.istad.corefeature.review.dto.ReviewRequest;
import co.istad.corefeature.review.dto.ReviewResponse;
import co.istad.corefeature.review.dto.ReviewSummaryResponse;
import co.istad.corefeature.review.dto.ReviewUpdateRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ReviewService {

    ReviewResponse create(ReviewRequest request);

    Page<ReviewResponse> findAll(Pageable pageable);

    ReviewResponse findById(Long id);

    Page<ReviewResponse> findByProductId(Long productId, Pageable pageable);

    ReviewSummaryResponse getProductReviewSummary(Long productId);

    Page<ReviewResponse> findByCustomerId(String customerId, Pageable pageable);

    ReviewResponse update(Long id, ReviewUpdateRequest request);

    ReviewResponse softDeleteById(Long id);

    void permanentDeleteById(Long id);
}
