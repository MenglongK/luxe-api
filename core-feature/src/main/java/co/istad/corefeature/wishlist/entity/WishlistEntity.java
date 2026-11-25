package co.istad.corefeature.wishlist.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "wishlists")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "items")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class WishlistEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @EqualsAndHashCode.Include
    private UUID id;

    @Column(name = "customer_id", nullable = false, unique = true, length = 64)
    private String customerId;

    @Column(length = 100)
    @Builder.Default
    private String name = "Default Wishlist";

    @OneToMany(mappedBy = "wishlist", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<WishlistItemEntity> items = new ArrayList<>();

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
        if (name == null || name.isBlank()) {
            name = "Default Wishlist";
        }
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public void addItem(WishlistItemEntity item) {
        items.add(item);
        item.setWishlist(this);
    }

    public void removeItem(WishlistItemEntity item) {
        items.remove(item);
        item.setWishlist(null);
    }
}
