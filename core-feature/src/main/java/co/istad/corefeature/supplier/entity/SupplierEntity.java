package co.istad.corefeature.supplier.entity;

import co.istad.corefeature.common.enums.PaymentTerms;
import co.istad.corefeature.common.enums.SupplierCategory;
import co.istad.corefeature.common.enums.SupplierStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "suppliers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class SupplierEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(nullable = false, unique = true, length = 60)
    private String code;

    @Column(nullable = false, length = 150)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private SupplierCategory category;

    @Column(name = "contact_person", nullable = false, length = 100)
    private String contactPerson;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Column(nullable = false, length = 50)
    private String phone;

    @Column(nullable = false, length = 255)
    private String address;

    @Column(nullable = false)
    @Builder.Default
    private Integer rating = 5;

    @Column(name = "lead_time_days", nullable = false)
    @Builder.Default
    private Integer leadTimeDays = 14;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_terms", nullable = false, length = 30)
    @Builder.Default
    private PaymentTerms paymentTerms = PaymentTerms.NET_30;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private SupplierStatus status = SupplierStatus.ACTIVE;

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
            status = SupplierStatus.ACTIVE;
        }
        if (paymentTerms == null) {
            paymentTerms = PaymentTerms.NET_30;
        }
        if (rating == null) {
            rating = 5;
        }
        if (leadTimeDays == null) {
            leadTimeDays = 14;
        }
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = LocalDateTime.now();
        if (code != null) {
            code = code.trim().toUpperCase();
        }
    }
}
