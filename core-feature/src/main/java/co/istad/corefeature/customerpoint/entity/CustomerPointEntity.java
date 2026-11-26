package co.istad.corefeature.customerpoint.entity;

import co.istad.corefeature.common.enums.CustomerTier;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "customer_points", indexes = {
        @Index(name = "idx_customer_points_customer_id", columnList = "customer_id", unique = true),
        @Index(name = "idx_customer_points_membership_number", columnList = "membership_number", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class CustomerPointEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "membership_number", nullable = false, unique = true, length = 32)
    private String membershipNumber;

    @Column(name = "customer_id", nullable = false, unique = true, length = 100)
    private String customerId;

    @Column(name = "total_points", nullable = false)
    @Builder.Default
    private Integer totalPoints = 0;

    @Column(name = "lifetime_points", nullable = false)
    @Builder.Default
    private Integer lifetimePoints = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private CustomerTier tier = CustomerTier.STANDARD;

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
        if (totalPoints == null) {
            totalPoints = 0;
        }
        if (lifetimePoints == null || lifetimePoints < totalPoints) {
            lifetimePoints = totalPoints;
        }
        if (tier == null) {
            tier = CustomerTier.fromPoints(lifetimePoints);
        }
        if (isActive == null) {
            isActive = true;
        }
        if (membershipNumber == null || membershipNumber.isBlank()) {
            membershipNumber = "MEM-" + UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase();
        }
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
