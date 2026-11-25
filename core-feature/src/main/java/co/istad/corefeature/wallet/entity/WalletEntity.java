package co.istad.corefeature.wallet.entity;

import co.istad.corefeature.common.enums.WalletStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "wallets", indexes = {
        @Index(name = "idx_wallets_customer_id", columnList = "customer_id", unique = true),
        @Index(name = "idx_wallets_wallet_number", columnList = "wallet_number", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class WalletEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @EqualsAndHashCode.Include
    private UUID id;

    @Column(name = "wallet_number", nullable = false, unique = true, length = 32)
    private String walletNumber;

    @Column(name = "customer_id", nullable = false, unique = true, length = 100)
    private String customerId;

    @Column(nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal balance = BigDecimal.ZERO;

    @Column(nullable = false, length = 10)
    @Builder.Default
    private String currency = "USD";

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private WalletStatus status = WalletStatus.ACTIVE;

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
        if (balance == null) {
            balance = BigDecimal.ZERO;
        }
        if (currency == null) {
            currency = "USD";
        }
        if (status == null) {
            status = WalletStatus.ACTIVE;
        }
        if (isActive == null) {
            isActive = true;
        }
        if (walletNumber == null) {
            walletNumber = "WAL-" + UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase();
        }
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
