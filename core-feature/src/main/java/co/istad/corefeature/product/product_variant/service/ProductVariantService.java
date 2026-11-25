package co.istad.corefeature.product.product_variant.service;

import co.istad.corefeature.product.product_variant.dto.ProductVariantRequest;
import co.istad.corefeature.product.product_variant.dto.ProductVariantResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface ProductVariantService {

    ProductVariantResponse createProductVariant(ProductVariantRequest request);

    ProductVariantResponse update(ProductVariantRequest request, Long id);

    Page<ProductVariantResponse> findAll(Pageable pageable);

    ProductVariantResponse findById(Long id);

    ProductVariantResponse findBySkuId(UUID skuId);

    List<ProductVariantResponse> findByProductId(Long productId);

    ProductVariantResponse softDeleteById(Long id);

    void permanentDeleteById(Long id);
}
