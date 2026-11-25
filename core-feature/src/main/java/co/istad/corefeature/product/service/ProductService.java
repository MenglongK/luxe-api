package co.istad.corefeature.product.service;

import co.istad.corefeature.product.dto.ProductRequest;
import co.istad.corefeature.product.dto.ProductResponse;
import co.istad.corefeature.product.product_variant.dto.ProductVariantResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface ProductService {

    ProductResponse createProduct(ProductRequest productRequest);

    ProductResponse updateProduct(ProductRequest productRequest, Long id);

    Page<ProductResponse> findAllProducts(Pageable pageable);

    ProductResponse findProductById(Long id);

    ProductResponse findByProductId(UUID productId);

    List<ProductVariantResponse> findVariantsByProductId(Long id);

    ProductResponse softDeleteById(Long id);

    void permanentDeleteById(Long id);
}
