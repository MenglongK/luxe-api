package co.istad.corefeature.wishlist.entity;

import co.istad.corefeature.product.product_variant.entity.ProductVariantEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "wishlist_items",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_wishlist_product_variant",
                        columnNames = {"wishlist_id", "product_variant_id"}
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"wishlist", "productVariant"})
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class WishlistItemEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "wishlist_id", nullable = false)
    private WishlistEntity wishlist;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_variant_id", nullable = false)
    private ProductVariantEntity productVariant;

    @Column(length = 255)
    private String note;

    @Column(name = "added_at", nullable = false)
    private LocalDateTime addedAt;

    @PrePersist
    void prePersist() {
        if (addedAt == null) {
            addedAt = LocalDateTime.now();
        }
    }
}
