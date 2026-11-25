package co.istad.corefeature.review.entity;

import co.istad.corefeature.common.enums.FitFeedback;
import co.istad.corefeature.product.entity.ProductEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "product_reviews")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "product")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ReviewEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private ProductEntity product;

    @Column(name = "customer_id", nullable = false)
    private String customerId;

    @Column(name = "author_name", nullable = false)
    private String authorName;

    @Column(nullable = false)
    private Integer rating;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String comment;

    @Column(name = "is_verified_buyer", nullable = false)
    @Builder.Default
    private Boolean isVerifiedBuyer = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "fit_feedback", length = 30)
    private FitFeedback fitFeedback;

    @Column(name = "size_purchased", length = 50)
    private String sizePurchased;

    @Column(name = "color_purchased", length = 50)
    private String colorPurchased;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) {
            createdAt = now;
        }
        updatedAt = now;
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
