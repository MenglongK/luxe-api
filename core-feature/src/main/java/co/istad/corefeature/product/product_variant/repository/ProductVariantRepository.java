package co.istad.corefeature.product.product_variant.repository;

import co.istad.corefeature.product.product_variant.entity.ProductVariantEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductVariantRepository extends JpaRepository<ProductVariantEntity, Long> {

    Optional<ProductVariantEntity> findBySkuId(UUID skuId);

    List<ProductVariantEntity> findByProductId(Long productId);

    List<ProductVariantEntity> findByProductIdAndIsActiveTrue(Long productId);

    Page<ProductVariantEntity> findByProductIdAndIsActiveTrue(Long productId, Pageable pageable);

    Page<ProductVariantEntity> findByIsActiveTrue(Pageable pageable);

    boolean existsBySkuId(UUID skuId);
}
