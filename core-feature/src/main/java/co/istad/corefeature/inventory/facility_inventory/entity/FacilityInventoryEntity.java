package co.istad.corefeature.inventory.facility_inventory.entity;

import co.istad.corefeature.facility.entity.FacilityEntity;
import co.istad.corefeature.product.entity.ProductVariantEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "facility_inventories", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"facility_id", "product_variant_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"facility", "productVariant"})
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class FacilityInventoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "facility_id", nullable = false)
    private FacilityEntity facility;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_variant_id", nullable = false)
    private ProductVariantEntity productVariant;

    @Column(nullable = false)
    @Builder.Default
    private Integer quantityOnHand = 0;

    @Column(nullable = false)
    @Builder.Default
    private Integer reservedQuantity = 0;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;
}
