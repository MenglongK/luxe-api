package co.istad.corefeature.wishlist.repository;

import co.istad.corefeature.wishlist.entity.WishlistItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface WishlistItemRepository extends JpaRepository<WishlistItemEntity, Long> {

    boolean existsByWishlistIdAndProductVariantId(UUID wishlistId, Long productVariantId);

    Optional<WishlistItemEntity> findByWishlistIdAndProductVariantId(UUID wishlistId, Long productVariantId);
}
