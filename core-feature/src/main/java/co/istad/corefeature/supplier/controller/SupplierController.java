package co.istad.corefeature.supplier.controller;

import co.istad.corefeature.common.enums.SupplierCategory;
import co.istad.corefeature.common.enums.SupplierStatus;
import co.istad.corefeature.common.response.ApiResponse;
import co.istad.corefeature.supplier.dto.SupplierRequest;
import co.istad.corefeature.supplier.dto.SupplierResponse;
import co.istad.corefeature.supplier.dto.SupplierUpdateRequest;
import co.istad.corefeature.supplier.service.SupplierService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@RequestMapping({"/api/v1/suppliers", "/api/v1/supplier"})
@RequiredArgsConstructor
@Tag(name = "Supplier Controller", description = "Endpoints for managing luxury fabric mills, leather tanneries, and artisan suppliers")
public class SupplierController {

    private final SupplierService supplierService;

    @PostMapping
    @Operation(summary = "Register supplier", description = "Creates a new artisan supplier or fabric mill record")
    public ResponseEntity<ApiResponse<SupplierResponse>> create(@Valid @RequestBody SupplierRequest request) {
        SupplierResponse response = supplierService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Supplier registered successfully", response));
    }

    @GetMapping
    @Operation(summary = "Get paginated suppliers", description = "Retrieves active suppliers with optional category filter, status filter, and search query")
    public ResponseEntity<ApiResponse<Page<SupplierResponse>>> findAll(
            @RequestParam(required = false) SupplierCategory category,
            @RequestParam(required = false) SupplierStatus status,
            @RequestParam(required = false) String search,
            @ParameterObject @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(ApiResponse.ok("Suppliers retrieved successfully", supplierService.findAll(category, status, search, pageable)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get supplier by ID", description = "Retrieves supplier details by ID")
    public ResponseEntity<ApiResponse<SupplierResponse>> findById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Supplier retrieved successfully", supplierService.findById(id)));
    }

    @GetMapping("/code/{code}")
    @Operation(summary = "Get supplier by code", description = "Retrieves supplier details by unique supplier code")
    public ResponseEntity<ApiResponse<SupplierResponse>> findByCode(@PathVariable String code) {
        return ResponseEntity.ok(ApiResponse.ok("Supplier retrieved successfully", supplierService.findByCode(code)));
    }

    @RequestMapping(value = "/{id}", method = {RequestMethod.PATCH, RequestMethod.PUT})
    @Operation(summary = "Update supplier", description = "Updates supplier contact details, lead time, rating, or payment terms")
    public ResponseEntity<ApiResponse<SupplierResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody SupplierUpdateRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.ok("Supplier updated successfully", supplierService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Soft delete supplier", description = "Marks a supplier as inactive")
    public ResponseEntity<ApiResponse<SupplierResponse>> softDelete(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Supplier soft deleted successfully", supplierService.softDeleteById(id)));
    }

    @DeleteMapping("/{id}/permanent")
    @Operation(summary = "Permanently delete supplier", description = "Hard deletes a supplier record from the database")
    public ResponseEntity<ApiResponse<Void>> permanentDelete(@PathVariable Long id) {
        supplierService.permanentDeleteById(id);
        return ResponseEntity.ok(ApiResponse.ok("Supplier permanently deleted successfully"));
    }
}
