package co.istad.corefeature.product.repository;

import co.istad.corefeature.product.entity.ProductVariantEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductVariantRepository extends JpaRepository<ProductVariantEntity, Long> {

    Optional<ProductVariantEntity> findBySkuId(UUID skuId);

    List<ProductVariantEntity> findByProductId(Long productId);
}
