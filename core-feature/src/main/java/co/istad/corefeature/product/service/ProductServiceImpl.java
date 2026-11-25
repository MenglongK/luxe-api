package co.istad.corefeature.product.service;

import co.istad.corefeature.brand.repository.BrandRepository;
import co.istad.corefeature.category.repository.CategoryRepository;
import co.istad.corefeature.common.enums.DiscountType;
import co.istad.corefeature.product.dto.ProductRequest;
import co.istad.corefeature.product.dto.ProductResponse;
import co.istad.corefeature.product.entity.ProductEntity;
import co.istad.corefeature.product.mapper.ProductMapper;
import co.istad.corefeature.product.product_variant.dto.ProductVariantResponse;
import co.istad.corefeature.product.product_variant.entity.ProductVariantEntity;
import co.istad.corefeature.product.product_variant.mapper.ProductVariantMapper;
import co.istad.corefeature.product.product_variant.repository.ProductVariantRepository;
import co.istad.corefeature.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final BrandRepository brandRepository;
    private final CategoryRepository categoryRepository;
    private final ProductVariantRepository productVariantRepository;
    private final ProductVariantMapper productVariantMapper;

    @Override
    @Transactional
    public ProductResponse createProduct(ProductRequest productRequest) {
        if (productRepository.existsByName(productRequest.name())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Product with name '" + productRequest.name() + "' already exists");
        }

        if (productRequest.brandId() != null && !brandRepository.existsById(productRequest.brandId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Brand not found with ID: " + productRequest.brandId());
        }

        if (productRequest.categoryId() != null && !categoryRepository.existsById(productRequest.categoryId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found with ID: " + productRequest.categoryId());
        }

        ProductEntity productEntity = productMapper.toEntity(productRequest);
        if (productEntity.getIsActive() == null) {
            productEntity.setIsActive(true);
        }
        if (productEntity.getDiscountType() == null) {
            productEntity.setDiscountType(DiscountType.NONE);
        }

        return productMapper.toResponse(productRepository.save(productEntity));
    }

    @Override
    @Transactional
    public ProductResponse updateProduct(ProductRequest productRequest, Long id) {
        ProductEntity productEntity = productRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found with ID: " + id));

        if (!productEntity.getName().equals(productRequest.name()) && productRepository.existsByName(productRequest.name())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Product with name '" + productRequest.name() + "' already exists");
        }

        if (productRequest.brandId() != null && !brandRepository.existsById(productRequest.brandId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Brand not found with ID: " + productRequest.brandId());
        }

        if (productRequest.categoryId() != null && !categoryRepository.existsById(productRequest.categoryId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found with ID: " + productRequest.categoryId());
        }

        productMapper.updateEntityFromDto(productRequest, productEntity);
        return productMapper.toResponse(productRepository.save(productEntity));
    }

    @Override
    public Page<ProductResponse> findAllProducts(Pageable pageable) {
        return productRepository.findByIsActiveTrue(pageable)
                .map(productMapper::toResponse);
    }

    @Override
    public ProductResponse findProductById(Long id) {
        return productRepository.findById(id)
                .map(productMapper::toResponse)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found with ID: " + id));
    }

    @Override
    public ProductResponse findByProductId(UUID productId) {
        return productRepository.findByProductId(productId)
                .map(productMapper::toResponse)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found with UUID: " + productId));
    }

    @Override
    public List<ProductVariantResponse> findVariantsByProductId(Long id) {
        if (!productRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found with ID: " + id);
        }
        List<ProductVariantEntity> variants = productVariantRepository.findByProductIdAndIsActiveTrue(id);
        return variants.stream()
                .map(productVariantMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public ProductResponse softDeleteById(Long id) {
        ProductEntity productEntity = productRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found with ID: " + id));
        productEntity.setIsActive(false);
        return productMapper.toResponse(productRepository.save(productEntity));
    }

    @Override
    @Transactional
    public void permanentDeleteById(Long id) {
        ProductEntity productEntity = productRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found with ID: " + id));
        productRepository.delete(productEntity);
    }
}
