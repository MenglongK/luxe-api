package co.istad.corefeature.brand.controller;

import co.istad.corefeature.brand.dto.BrandRequest;
import co.istad.corefeature.brand.dto.BrandResponse;
import co.istad.corefeature.brand.service.BrandService;
import co.istad.corefeature.common.response.ApiResponse;
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

@RestController
@RequestMapping("/api/v1/brands")
@RequiredArgsConstructor
public class BrandController {
    private final BrandService brandService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<BrandResponse>>> findAll(
            @ParameterObject @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(ApiResponse.ok("Brands retrieved successfully", brandService.findAllBrands(pageable)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BrandResponse>> findBrandById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Brands retrieved successfully", brandService.findBrandById(id)));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<BrandResponse>> updateBrand(
            @PathVariable Long id,
            @Valid @RequestBody BrandRequest brandRequest
    ) {
        return ResponseEntity.ok(ApiResponse.ok("Brands updated successfully", brandService.updateBrand(brandRequest, id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<BrandResponse>> createBrand(
            @Valid @RequestBody BrandRequest brandRequest
    ){
        BrandResponse response = brandService.createBrand(brandRequest);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Brand created successfully", response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<BrandResponse>> softDeleteBrand(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(ApiResponse.ok("Brand soft deleted successfully", brandService.softDeleteById(id)));
    }

    @DeleteMapping("/{id}/permanent")
    public ResponseEntity<ApiResponse<Void>> permanentDeleteBrand(
            @PathVariable Long id
    ) {
        brandService.permanentDeleteById(id);
        return ResponseEntity.ok(ApiResponse.ok("Brand permanently deleted successfully"));
    }
}
