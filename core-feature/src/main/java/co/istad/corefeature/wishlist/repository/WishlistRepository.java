package co.istad.corefeature.wishlist.repository;

import co.istad.corefeature.wishlist.entity.WishlistEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface WishlistRepository extends JpaRepository<WishlistEntity, UUID> {

    Optional<WishlistEntity> findByIdAndIsActiveTrue(UUID id);

    Optional<WishlistEntity> findByCustomerIdAndIsActiveTrue(String customerId);

    boolean existsByCustomerIdAndIsActiveTrue(String customerId);

    Page<WishlistEntity> findByIsActiveTrue(Pageable pageable);
}
