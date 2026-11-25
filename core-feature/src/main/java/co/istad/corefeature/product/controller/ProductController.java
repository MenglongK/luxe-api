package co.istad.corefeature.product.controller;

import co.istad.corefeature.common.response.ApiResponse;
import co.istad.corefeature.product.dto.ProductRequest;
import co.istad.corefeature.product.dto.ProductResponse;
import co.istad.corefeature.product.product_variant.dto.ProductVariantResponse;
import co.istad.corefeature.product.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping({"/api/v1/products", "/api/v1/product"})
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @PostMapping
    public ResponseEntity<ApiResponse<ProductResponse>> createProduct(
            @Valid @RequestBody ProductRequest productRequest
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Product created successfully", productService.createProduct(productRequest)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<ProductResponse>>> findAll(
            @ParameterObject @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(ApiResponse.ok("Products retrieved successfully", productService.findAllProducts(pageable)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponse>> findById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Product retrieved successfully", productService.findProductById(id)));
    }

    @GetMapping("/uuid/{productId}")
    public ResponseEntity<ApiResponse<ProductResponse>> findByProductId(@PathVariable UUID productId) {
        return ResponseEntity.ok(ApiResponse.ok("Product retrieved successfully", productService.findByProductId(productId)));
    }

    @GetMapping("/{id}/variants")
    public ResponseEntity<ApiResponse<List<ProductVariantResponse>>> findVariantsByProductId(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Product variants retrieved successfully", productService.findVariantsByProductId(id)));
    }

    @RequestMapping(value = "/{id}", method = {RequestMethod.PATCH, RequestMethod.PUT})
    public ResponseEntity<ApiResponse<ProductResponse>> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductRequest productRequest
    ) {
        return ResponseEntity.ok(ApiResponse.ok("Product updated successfully", productService.updateProduct(productRequest, id)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponse>> softDeleteProduct(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Product soft deleted successfully", productService.softDeleteById(id)));
    }

    @DeleteMapping("/{id}/permanent")
    public ResponseEntity<ApiResponse<Void>> permanentDeleteProduct(@PathVariable Long id) {
        productService.permanentDeleteById(id);
        return ResponseEntity.ok(ApiResponse.ok("Product permanently deleted successfully"));
    }
}
