package co.istad.corefeature.review.service;

import co.istad.corefeature.common.enums.FitFeedback;
import co.istad.corefeature.common.enums.OrderStatus;
import co.istad.corefeature.order.entity.OrderEntity;
import co.istad.corefeature.order.order_item.entity.OrderItemEntity;
import co.istad.corefeature.order.repository.OrderRepository;
import co.istad.corefeature.product.entity.ProductEntity;
import co.istad.corefeature.product.product_variant.entity.ProductVariantEntity;
import co.istad.corefeature.product.repository.ProductRepository;
import co.istad.corefeature.review.dto.ReviewRequest;
import co.istad.corefeature.review.dto.ReviewResponse;
import co.istad.corefeature.review.dto.ReviewSummaryResponse;
import co.istad.corefeature.review.dto.ReviewUpdateRequest;
import co.istad.corefeature.review.entity.ReviewEntity;
import co.istad.corefeature.review.mapper.ReviewMapper;
import co.istad.corefeature.review.mapper.ReviewMapperImpl;
import co.istad.corefeature.review.repository.ReviewRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewServiceImplTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private OrderRepository orderRepository;

    private ReviewMapper reviewMapper;
    private ReviewService reviewService;

    @BeforeEach
    void setUp() {
        reviewMapper = new ReviewMapperImpl();
        reviewService = new ReviewServiceImpl(
                reviewRepository,
                productRepository,
                orderRepository,
                reviewMapper
        );
    }

    @Test
    @DisplayName("Should successfully submit a review and detect verified buyer status")
    void createReview_verifiedBuyer_success() {
        Long productId = 1L;
        String customerId = "cust-vip-001";

        ProductEntity product = ProductEntity.builder()
                .id(productId)
                .name("Vicuna Double-Breasted Blazer")
                .isActive(true)
                .build();

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(reviewRepository.existsByCustomerIdAndProductIdAndIsActiveTrue(customerId, productId)).thenReturn(false);

        ProductVariantEntity variant = ProductVariantEntity.builder()
                .id(10L)
                .product(product)
                .build();

        OrderItemEntity orderItem = OrderItemEntity.builder()
                .productVariant(variant)
                .build();

        OrderEntity completedOrder = OrderEntity.builder()
                .customerId(customerId)
                .status(OrderStatus.COMPLETED)
                .items(List.of(orderItem))
                .isActive(true)
                .build();

        Page<OrderEntity> orderPage = new PageImpl<>(List.of(completedOrder));
        when(orderRepository.findByCustomerIdAndIsActiveTrue(eq(customerId), any(Pageable.class))).thenReturn(orderPage);

        when(reviewRepository.save(any(ReviewEntity.class))).thenAnswer(invocation -> {
            ReviewEntity entity = invocation.getArgument(0);
            entity.setId(100L);
            return entity;
        });

        ReviewRequest request = ReviewRequest.builder()
                .productId(productId)
                .customerId(customerId)
                .authorName("Lady Genevieve")
                .rating(5)
                .title("Unmatched drape and craftsmanship")
                .comment("The vicuna fiber quality is breathtaking.")
                .fitFeedback(FitFeedback.TRUE_TO_SIZE)
                .sizePurchased("50R")
                .colorPurchased("Charcoal Melange")
                .build();

        ReviewResponse response = reviewService.create(request);

        assertNotNull(response);
        assertEquals(100L, response.id());
        assertEquals(productId, response.productId());
        assertEquals(5, response.rating());
        assertTrue(response.isVerifiedBuyer());
        assertEquals(FitFeedback.TRUE_TO_SIZE, response.fitFeedback());
        verify(reviewRepository).save(any(ReviewEntity.class));
    }

    @Test
    @DisplayName("Should submit review as unverified when customer has no completed order for product")
    void createReview_unverifiedBuyer_success() {
        Long productId = 2L;
        String customerId = "cust-guest-999";

        ProductEntity product = ProductEntity.builder()
                .id(productId)
                .name("Cashmere Turtleneck")
                .isActive(true)
                .build();

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(reviewRepository.existsByCustomerIdAndProductIdAndIsActiveTrue(customerId, productId)).thenReturn(false);
        when(orderRepository.findByCustomerIdAndIsActiveTrue(eq(customerId), any(Pageable.class))).thenReturn(Page.empty());

        when(reviewRepository.save(any(ReviewEntity.class))).thenAnswer(invocation -> {
            ReviewEntity entity = invocation.getArgument(0);
            entity.setId(101L);
            return entity;
        });

        ReviewRequest request = ReviewRequest.builder()
                .productId(productId)
                .customerId(customerId)
                .authorName("Alexander M.")
                .rating(4)
                .title("Very soft knit")
                .comment("Extremely comfortable for winter galas.")
                .fitFeedback(FitFeedback.RUNS_SLIGHTLY_LARGE)
                .build();

        ReviewResponse response = reviewService.create(request);

        assertNotNull(response);
        assertFalse(response.isVerifiedBuyer());
    }

    @Test
    @DisplayName("Should throw 409 CONFLICT if customer already reviewed product")
    void createReview_alreadyReviewed_throwsConflict() {
        Long productId = 1L;
        String customerId = "cust-vip-001";

        ProductEntity product = ProductEntity.builder().id(productId).isActive(true).build();
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(reviewRepository.existsByCustomerIdAndProductIdAndIsActiveTrue(customerId, productId)).thenReturn(true);

        ReviewRequest request = ReviewRequest.builder()
                .productId(productId)
                .customerId(customerId)
                .authorName("Lady Genevieve")
                .rating(5)
                .title("Duplicate")
                .comment("Duplicate comment")
                .build();

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> reviewService.create(request));
        assertEquals(409, ex.getStatusCode().value());
        assertTrue(ex.getReason().contains("already reviewed"));
    }

    @Test
    @DisplayName("Should throw 400 BAD_REQUEST when rating is out of bounds")
    void createReview_invalidRating_throwsBadRequest() {
        Long productId = 1L;
        String customerId = "cust-001";

        ProductEntity product = ProductEntity.builder().id(productId).isActive(true).build();
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(reviewRepository.existsByCustomerIdAndProductIdAndIsActiveTrue(customerId, productId)).thenReturn(false);

        ReviewRequest request = ReviewRequest.builder()
                .productId(productId)
                .customerId(customerId)
                .authorName("Client")
                .rating(6)
                .title("Invalid")
                .comment("Comment")
                .build();

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> reviewService.create(request));
        assertEquals(400, ex.getStatusCode().value());
        assertTrue(ex.getReason().contains("Rating must be between 1 and 5"));
    }

    @Test
    @DisplayName("Should correctly calculate review aggregate statistics for product")
    void getProductReviewSummary_success() {
        Long productId = 1L;
        when(productRepository.existsById(productId)).thenReturn(true);

        ProductEntity product = ProductEntity.builder().id(productId).name("Blazer").build();
        List<ReviewEntity> list = new ArrayList<>();
        list.add(ReviewEntity.builder().id(1L).product(product).rating(5).fitFeedback(FitFeedback.TRUE_TO_SIZE).build());
        list.add(ReviewEntity.builder().id(2L).product(product).rating(5).fitFeedback(FitFeedback.TRUE_TO_SIZE).build());
        list.add(ReviewEntity.builder().id(3L).product(product).rating(4).fitFeedback(FitFeedback.RUNS_SLIGHTLY_SMALL).build());
        list.add(ReviewEntity.builder().id(4L).product(product).rating(5).fitFeedback(FitFeedback.TRUE_TO_SIZE).build());

        when(reviewRepository.findByProductIdAndIsActiveTrue(productId)).thenReturn(list);

        ReviewSummaryResponse summary = reviewService.getProductReviewSummary(productId);

        assertNotNull(summary);
        assertEquals(productId, summary.productId());
        assertEquals(4L, summary.totalReviews());
        assertEquals(4.8, summary.averageRating());
        assertEquals(3L, summary.ratingDistribution().get(5));
        assertEquals(1L, summary.ratingDistribution().get(4));
        assertEquals(0L, summary.ratingDistribution().get(1));
        assertEquals(3L, summary.fitFeedbackBreakdown().get("TRUE_TO_SIZE"));
        assertEquals(1L, summary.fitFeedbackBreakdown().get("RUNS_SLIGHTLY_SMALL"));
    }

    @Test
    @DisplayName("Should update review details successfully")
    void updateReview_success() {
        Long reviewId = 1L;
        ProductEntity product = ProductEntity.builder().id(10L).name("Trench Coat").build();
        ReviewEntity existing = ReviewEntity.builder()
                .id(reviewId)
                .product(product)
                .rating(4)
                .title("Old Title")
                .comment("Old Comment")
                .isActive(true)
                .build();

        when(reviewRepository.findByIdAndIsActiveTrue(reviewId)).thenReturn(Optional.of(existing));
        when(reviewRepository.save(any(ReviewEntity.class))).thenAnswer(i -> i.getArgument(0));

        ReviewUpdateRequest updateRequest = ReviewUpdateRequest.builder()
                .rating(5)
                .title("Revised: Perfect Trench")
                .comment("After tailoring the sleeves, it is 5 stars.")
                .fitFeedback(FitFeedback.TRUE_TO_SIZE)
                .build();

        ReviewResponse response = reviewService.update(reviewId, updateRequest);

        assertNotNull(response);
        assertEquals(5, response.rating());
        assertEquals("Revised: Perfect Trench", response.title());
        assertEquals("After tailoring the sleeves, it is 5 stars.", response.comment());
        verify(reviewRepository).save(existing);
    }

    @Test
    @DisplayName("Should soft delete an active review")
    void softDeleteById_success() {
        Long reviewId = 1L;
        ReviewEntity existing = ReviewEntity.builder()
                .id(reviewId)
                .isActive(true)
                .build();

        when(reviewRepository.findByIdAndIsActiveTrue(reviewId)).thenReturn(Optional.of(existing));
        when(reviewRepository.save(any(ReviewEntity.class))).thenAnswer(i -> i.getArgument(0));

        ReviewResponse response = reviewService.softDeleteById(reviewId);

        assertNotNull(response);
        assertFalse(existing.getIsActive());
        verify(reviewRepository).save(existing);
    }

    @Test
    @DisplayName("Should successfully insert and paginate 10 distinct reviews")
    void findAll_tenReviews_success() {
        ProductEntity product = ProductEntity.builder().id(1L).name("Vicuna Blazer").build();
        List<ReviewEntity> reviews = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            reviews.add(ReviewEntity.builder()
                    .id((long) i)
                    .product(product)
                    .customerId("cust-vip-" + i)
                    .authorName("VIP Client " + i)
                    .rating(i == 6 ? 4 : 5)
                    .title("Review Title " + i)
                    .comment("Review Comment " + i)
                    .isVerifiedBuyer(true)
                    .fitFeedback(FitFeedback.TRUE_TO_SIZE)
                    .isActive(true)
                    .build());
        }

        Pageable pageable = PageRequest.of(0, 10);
        Page<ReviewEntity> page = new PageImpl<>(reviews, pageable, 10);

        when(reviewRepository.findByIsActiveTrue(pageable)).thenReturn(page);

        Page<ReviewResponse> result = reviewService.findAll(pageable);

        assertNotNull(result);
        assertEquals(10, result.getTotalElements());
        assertEquals(10, result.getContent().size());
        assertEquals("VIP Client 1", result.getContent().get(0).authorName());
        assertEquals("VIP Client 10", result.getContent().get(9).authorName());
    }
}
