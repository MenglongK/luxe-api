package co.istad.corefeature.product.repository;

import co.istad.corefeature.product.entity.ProductEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ProductRepository extends JpaRepository<ProductEntity, Long> {

    Optional<ProductEntity> findByProductId(UUID productId);

    boolean existsByProductId(UUID productId);
}
