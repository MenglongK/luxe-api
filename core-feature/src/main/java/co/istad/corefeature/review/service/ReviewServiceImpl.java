package co.istad.corefeature.review.service;

import co.istad.corefeature.common.enums.OrderStatus;
import co.istad.corefeature.order.repository.OrderRepository;
import co.istad.corefeature.product.entity.ProductEntity;
import co.istad.corefeature.product.repository.ProductRepository;
import co.istad.corefeature.review.dto.ReviewRequest;
import co.istad.corefeature.review.dto.ReviewResponse;
import co.istad.corefeature.review.dto.ReviewSummaryResponse;
import co.istad.corefeature.review.dto.ReviewUpdateRequest;
import co.istad.corefeature.review.entity.ReviewEntity;
import co.istad.corefeature.review.mapper.ReviewMapper;
import co.istad.corefeature.review.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final ReviewMapper reviewMapper;

    @Override
    @Transactional
    public ReviewResponse create(ReviewRequest request) {
        ProductEntity product = productRepository.findById(request.productId())
                .filter(ProductEntity::getIsActive)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found or inactive: " + request.productId()));

        if (reviewRepository.existsByCustomerIdAndProductIdAndIsActiveTrue(request.customerId(), request.productId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Customer has already reviewed product: " + request.productId());
        }

        if (request.rating() < 1 || request.rating() > 5) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Rating must be between 1 and 5");
        }

        boolean isVerifiedBuyer = orderRepository.findByCustomerIdAndIsActiveTrue(request.customerId(), PageRequest.of(0, 100))
                .stream()
                .filter(order -> order.getStatus() == OrderStatus.COMPLETED)
                .flatMap(order -> order.getItems().stream())
                .anyMatch(item -> item.getProductVariant() != null
                        && item.getProductVariant().getProduct() != null
                        && item.getProductVariant().getProduct().getId().equals(request.productId()));

        ReviewEntity review = reviewMapper.toEntity(request);
        review.setProduct(product);
        review.setIsVerifiedBuyer(isVerifiedBuyer);
        review.setIsActive(true);

        ReviewEntity saved = reviewRepository.save(review);
        return reviewMapper.toResponse(saved);
    }

    @Override
    public Page<ReviewResponse> findAll(Pageable pageable) {
        return reviewRepository.findByIsActiveTrue(pageable).map(reviewMapper::toResponse);
    }

    @Override
    public ReviewResponse findById(Long id) {
        return reviewRepository.findByIdAndIsActiveTrue(id)
                .map(reviewMapper::toResponse)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Review not found: " + id));
    }

    @Override
    public Page<ReviewResponse> findByProductId(Long productId, Pageable pageable) {
        if (!productRepository.existsById(productId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found: " + productId);
        }
        return reviewRepository.findByProductIdAndIsActiveTrue(productId, pageable).map(reviewMapper::toResponse);
    }

    @Override
    public ReviewSummaryResponse getProductReviewSummary(Long productId) {
        if (!productRepository.existsById(productId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found: " + productId);
        }

        List<ReviewEntity> reviews = reviewRepository.findByProductIdAndIsActiveTrue(productId);
        Map<Integer, Long> ratingDistribution = new HashMap<>();
        for (int r = 1; r <= 5; r++) {
            ratingDistribution.put(r, 0L);
        }

        Map<String, Long> fitBreakdown = new HashMap<>();

        if (reviews.isEmpty()) {
            return ReviewSummaryResponse.builder()
                    .productId(productId)
                    .averageRating(0.0)
                    .totalReviews(0L)
                    .ratingDistribution(ratingDistribution)
                    .fitFeedbackBreakdown(fitBreakdown)
                    .build();
        }

        for (ReviewEntity r : reviews) {
            ratingDistribution.put(r.getRating(), ratingDistribution.getOrDefault(r.getRating(), 0L) + 1L);
            if (r.getFitFeedback() != null) {
                fitBreakdown.put(r.getFitFeedback().name(), fitBreakdown.getOrDefault(r.getFitFeedback().name(), 0L) + 1L);
            }
        }

        double rawAvg = reviews.stream().mapToInt(ReviewEntity::getRating).average().orElse(0.0);
        double roundedAvg = BigDecimal.valueOf(rawAvg).setScale(1, RoundingMode.HALF_UP).doubleValue();

        return ReviewSummaryResponse.builder()
                .productId(productId)
                .averageRating(roundedAvg)
                .totalReviews((long) reviews.size())
                .ratingDistribution(ratingDistribution)
                .fitFeedbackBreakdown(fitBreakdown)
                .build();
    }

    @Override
    public Page<ReviewResponse> findByCustomerId(String customerId, Pageable pageable) {
        return reviewRepository.findByCustomerIdAndIsActiveTrue(customerId, pageable).map(reviewMapper::toResponse);
    }

    @Override
    @Transactional
    public ReviewResponse update(Long id, ReviewUpdateRequest request) {
        ReviewEntity review = reviewRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Review not found: " + id));

        if (request.rating() != null && (request.rating() < 1 || request.rating() > 5)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Rating must be between 1 and 5");
        }

        reviewMapper.updateEntityFromRequest(request, review);
        return reviewMapper.toResponse(reviewRepository.save(review));
    }

    @Override
    @Transactional
    public ReviewResponse softDeleteById(Long id) {
        ReviewEntity review = reviewRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Review not found: " + id));

        review.setIsActive(false);
        return reviewMapper.toResponse(reviewRepository.save(review));
    }

    @Override
    @Transactional
    public void permanentDeleteById(Long id) {
        ReviewEntity review = reviewRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Review not found: " + id));
        reviewRepository.delete(review);
    }
}
