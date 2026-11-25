package co.istad.corefeature.customerpoint.entity;

import co.istad.corefeature.common.enums.PointTransactionType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "customer_point_transactions", indexes = {
        @Index(name = "idx_cpt_customer_id", columnList = "customer_id"),
        @Index(name = "idx_cpt_point_id", columnList = "customer_point_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class CustomerPointTransactionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "customer_point_id", nullable = false)
    private Long customerPointId;

    @Column(name = "customer_id", nullable = false, length = 100)
    private String customerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false, length = 30)
    private PointTransactionType transactionType;

    @Column(nullable = false)
    private Integer points;

    @Column(name = "balance_after", nullable = false)
    private Integer balanceAfter;

    @Column(name = "reference_id", length = 100)
    private String referenceId;

    @Column(length = 255)
    private String description;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
