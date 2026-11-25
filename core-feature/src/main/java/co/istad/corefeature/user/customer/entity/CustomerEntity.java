package co.istad.corefeature.user.customer.entity;

import co.istad.corefeature.common.enums.CustomerTier;
import co.istad.corefeature.common.enums.Gender;
import co.istad.corefeature.facility.entity.Address;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "customers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class CustomerEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @EqualsAndHashCode.Include
    private UUID id;

    @Column(name = "customer_code", nullable = false, unique = true, length = 64)
    private String customerCode;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Column(nullable = false, length = 50)
    private String phone;

    @Column(name = "avatar_url")
    private String avatarUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private CustomerTier tier = CustomerTier.STANDARD;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private Gender gender;

    @Embedded
    private Address primaryAddress;

    @Column(name = "bespoke_notes", columnDefinition = "TEXT")
    private String bespokeNotes;

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
        if (customerCode == null || customerCode.isBlank()) {
            customerCode = "LX-CUST-" + System.currentTimeMillis();
        }
        if (tier == null) {
            tier = CustomerTier.STANDARD;
        }
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
