package co.istad.corefeature.product.product_variant.service;

import co.istad.corefeature.product.entity.ProductEntity;
import co.istad.corefeature.product.product_variant.dto.ProductVariantRequest;
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
public class ProductVariantServiceImpl implements ProductVariantService {

    private final ProductVariantRepository productVariantRepository;
    private final ProductRepository productRepository;
    private final ProductVariantMapper productVariantMapper;

    @Override
    @Transactional
    public ProductVariantResponse createProductVariant(ProductVariantRequest request) {
        ProductEntity product = productRepository.findById(request.productId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found with ID: " + request.productId()));

        if (Boolean.FALSE.equals(product.getIsActive())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot create variant for an inactive product");
        }

        ProductVariantEntity entity = productVariantMapper.toEntity(request);
        entity.setProduct(product);
        if (entity.getIsActive() == null) {
            entity.setIsActive(true);
        }

        return productVariantMapper.toResponse(productVariantRepository.save(entity));
    }

    @Override
    @Transactional
    public ProductVariantResponse update(ProductVariantRequest request, Long id) {
        ProductVariantEntity entity = productVariantRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product variant not found with ID: " + id));

        if (request.productId() != null && !request.productId().equals(entity.getProduct().getId())) {
            ProductEntity newProduct = productRepository.findById(request.productId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found with ID: " + request.productId()));
            entity.setProduct(newProduct);
        }

        productVariantMapper.updateEntityFromDto(request, entity);
        return productVariantMapper.toResponse(productVariantRepository.save(entity));
    }

    @Override
    public Page<ProductVariantResponse> findAll(Pageable pageable) {
        return productVariantRepository.findByIsActiveTrue(pageable)
                .map(productVariantMapper::toResponse);
    }

    @Override
    public ProductVariantResponse findById(Long id) {
        return productVariantRepository.findById(id)
                .map(productVariantMapper::toResponse)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product variant not found with ID: " + id));
    }

    @Override
    public ProductVariantResponse findBySkuId(UUID skuId) {
        return productVariantRepository.findBySkuId(skuId)
                .map(productVariantMapper::toResponse)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product variant not found with SKU ID: " + skuId));
    }

    @Override
    public List<ProductVariantResponse> findByProductId(Long productId) {
        if (!productRepository.existsById(productId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found with ID: " + productId);
        }
        return productVariantRepository.findByProductIdAndIsActiveTrue(productId)
                .stream()
                .map(productVariantMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public ProductVariantResponse softDeleteById(Long id) {
        ProductVariantEntity entity = productVariantRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product variant not found with ID: " + id));
        entity.setIsActive(false);
        return productVariantMapper.toResponse(productVariantRepository.save(entity));
    }

    @Override
    @Transactional
    public void permanentDeleteById(Long id) {
        ProductVariantEntity entity = productVariantRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product variant not found with ID: " + id));
        productVariantRepository.delete(entity);
    }
}
