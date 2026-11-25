package co.istad.corefeature.coupon.entity;

import co.istad.corefeature.common.enums.CouponStatus;
import co.istad.corefeature.common.enums.DiscountType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "coupons")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class CouponEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @EqualsAndHashCode.Include
    private UUID id;

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(nullable = false, length = 255)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "discount_type", nullable = false, length = 30)
    private DiscountType discountType;

    @Column(name = "discount_value", nullable = false, precision = 12, scale = 2)
    private BigDecimal discountValue;

    @Column(name = "min_spend", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal minSpend = BigDecimal.ZERO;

    @Column(name = "max_discount", precision = 12, scale = 2)
    private BigDecimal maxDiscount;

    @Column(name = "usage_count", nullable = false)
    @Builder.Default
    private Integer usageCount = 0;

    @Column(name = "usage_limit")
    private Integer usageLimit;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private CouponStatus status = CouponStatus.ACTIVE;

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
        if (code != null) {
            code = code.trim().toUpperCase();
        }
        if (status == null) {
            status = CouponStatus.ACTIVE;
        }
        if (minSpend == null) {
            minSpend = BigDecimal.ZERO;
        }
        if (usageCount == null) {
            usageCount = 0;
        }
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = LocalDateTime.now();
        if (code != null) {
            code = code.trim().toUpperCase();
        }
    }

    public boolean isCurrentlyUsable() {
        if (!Boolean.TRUE.equals(isActive) || status != CouponStatus.ACTIVE) {
            return false;
        }
        LocalDate today = LocalDate.now();
        if (startDate != null && today.isBefore(startDate)) {
            return false;
        }
        if (expiryDate != null && today.isAfter(expiryDate)) {
            return false;
        }
        return usageLimit == null || usageCount < usageLimit;
    }

    public BigDecimal calculateDiscount(BigDecimal cartSubtotal) {
        if (cartSubtotal == null || cartSubtotal.compareTo(minSpend) < 0 || !isCurrentlyUsable()) {
            return BigDecimal.ZERO;
        }

        BigDecimal discount = BigDecimal.ZERO;
        if (discountType == DiscountType.PERCENTAGE) {
            discount = cartSubtotal.multiply(discountValue)
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            if (maxDiscount != null && maxDiscount.compareTo(BigDecimal.ZERO) > 0 && discount.compareTo(maxDiscount) > 0) {
                discount = maxDiscount;
            }
        } else if (discountType == DiscountType.FIXED_AMOUNT) {
            discount = discountValue.min(cartSubtotal);
        }
        return discount;
    }
}
