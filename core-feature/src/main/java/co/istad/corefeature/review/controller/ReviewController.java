package co.istad.corefeature.review.controller;

import co.istad.corefeature.common.response.ApiResponse;
import co.istad.corefeature.review.dto.ReviewRequest;
import co.istad.corefeature.review.dto.ReviewResponse;
import co.istad.corefeature.review.dto.ReviewSummaryResponse;
import co.istad.corefeature.review.dto.ReviewUpdateRequest;
import co.istad.corefeature.review.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping({"/api/v1/reviews", "/api/v1/review"})
@RequiredArgsConstructor
@Tag(name = "Review Controller", description = "Endpoints for managing luxury product ratings, client reviews, and fit feedback")
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    @Operation(summary = "Create product review", description = "Submits a review for a luxury product and auto-verifies buyer status")
    public ResponseEntity<ApiResponse<ReviewResponse>> create(@Valid @RequestBody ReviewRequest request) {
        ReviewResponse response = reviewService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Review submitted successfully", response));
    }

    @GetMapping
    @Operation(summary = "Get paginated reviews", description = "Retrieves active product reviews with pagination and sorting")
    public ResponseEntity<ApiResponse<Page<ReviewResponse>>> findAll(
            @ParameterObject @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(ApiResponse.ok("Reviews retrieved successfully", reviewService.findAll(pageable)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get review by ID", description = "Retrieves an active review by its numeric ID")
    public ResponseEntity<ApiResponse<ReviewResponse>> findById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Review retrieved successfully", reviewService.findById(id)));
    }

    @GetMapping("/product/{productId}")
    @Operation(summary = "Get reviews for a product", description = "Retrieves paginated reviews for a specific luxury product")
    public ResponseEntity<ApiResponse<Page<ReviewResponse>>> findByProductId(
            @PathVariable Long productId,
            @ParameterObject @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(ApiResponse.ok("Product reviews retrieved successfully", reviewService.findByProductId(productId, pageable)));
    }

    @GetMapping("/product/{productId}/summary")
    @Operation(summary = "Get product review summary", description = "Retrieves aggregate statistics including average score, count, star distribution, and fit breakdown")
    public ResponseEntity<ApiResponse<ReviewSummaryResponse>> getProductReviewSummary(@PathVariable Long productId) {
        return ResponseEntity.ok(ApiResponse.ok("Product review summary retrieved successfully", reviewService.getProductReviewSummary(productId)));
    }

    @GetMapping("/customer/{customerId}")
    @Operation(summary = "Get reviews by customer ID", description = "Retrieves paginated reviews submitted by a specific customer")
    public ResponseEntity<ApiResponse<Page<ReviewResponse>>> findByCustomerId(
            @PathVariable String customerId,
            @ParameterObject @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(ApiResponse.ok("Customer reviews retrieved successfully", reviewService.findByCustomerId(customerId, pageable)));
    }

    @RequestMapping(value = "/{id}", method = {RequestMethod.PATCH, RequestMethod.PUT})
    @Operation(summary = "Update review", description = "Updates review details such as rating, title, comment, and fit feedback")
    public ResponseEntity<ApiResponse<ReviewResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody ReviewUpdateRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.ok("Review updated successfully", reviewService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Soft delete review", description = "Marks a review as inactive")
    public ResponseEntity<ApiResponse<ReviewResponse>> softDelete(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Review soft deleted successfully", reviewService.softDeleteById(id)));
    }

    @DeleteMapping("/{id}/permanent")
    @Operation(summary = "Permanently delete review", description = "Hard deletes a review from the database")
    public ResponseEntity<ApiResponse<Void>> permanentDelete(@PathVariable Long id) {
        reviewService.permanentDeleteById(id);
        return ResponseEntity.ok(ApiResponse.ok("Review permanently deleted successfully"));
    }
}
