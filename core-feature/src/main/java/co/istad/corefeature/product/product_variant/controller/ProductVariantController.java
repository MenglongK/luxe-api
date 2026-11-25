package co.istad.corefeature.product.product_variant.controller;

import co.istad.corefeature.common.response.ApiResponse;
import co.istad.corefeature.product.product_variant.dto.ProductVariantRequest;
import co.istad.corefeature.product.product_variant.dto.ProductVariantResponse;
import co.istad.corefeature.product.product_variant.service.ProductVariantService;
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
@RequestMapping({"/api/v1/product-variants", "/api/v1/product-variant"})
@RequiredArgsConstructor
public class ProductVariantController {

    private final ProductVariantService productVariantService;

    @PostMapping
    public ResponseEntity<ApiResponse<ProductVariantResponse>> createProductVariant(
            @Valid @RequestBody ProductVariantRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Product variant created successfully", productVariantService.createProductVariant(request)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<ProductVariantResponse>>> findAll(
            @ParameterObject @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(ApiResponse.ok("Product variants retrieved successfully", productVariantService.findAll(pageable)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductVariantResponse>> findById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Product variant retrieved successfully", productVariantService.findById(id)));
    }

    @GetMapping("/sku/{skuId}")
    public ResponseEntity<ApiResponse<ProductVariantResponse>> findBySkuId(@PathVariable UUID skuId) {
        return ResponseEntity.ok(ApiResponse.ok("Product variant retrieved successfully", productVariantService.findBySkuId(skuId)));
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<ApiResponse<List<ProductVariantResponse>>> findByProductId(@PathVariable Long productId) {
        return ResponseEntity.ok(ApiResponse.ok("Product variants retrieved successfully", productVariantService.findByProductId(productId)));
    }

    @RequestMapping(value = "/{id}", method = {RequestMethod.PATCH, RequestMethod.PUT})
    public ResponseEntity<ApiResponse<ProductVariantResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody ProductVariantRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.ok("Product variant updated successfully", productVariantService.update(request, id)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductVariantResponse>> softDelete(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Product variant soft deleted successfully", productVariantService.softDeleteById(id)));
    }

    @DeleteMapping("/{id}/permanent")
    public ResponseEntity<ApiResponse<Void>> permanentDelete(@PathVariable Long id) {
        productVariantService.permanentDeleteById(id);
        return ResponseEntity.ok(ApiResponse.ok("Product variant permanently deleted successfully"));
    }
}
