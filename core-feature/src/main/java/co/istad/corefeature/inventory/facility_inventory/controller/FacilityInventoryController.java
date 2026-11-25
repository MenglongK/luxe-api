package co.istad.corefeature.inventory.facility_inventory.controller;

import co.istad.corefeature.common.response.ApiResponse;
import co.istad.corefeature.inventory.facility_inventory.dto.FacilityInventoryRequest;
import co.istad.corefeature.inventory.facility_inventory.dto.FacilityInventoryResponse;
import co.istad.corefeature.inventory.facility_inventory.dto.StockRequest;
import co.istad.corefeature.inventory.facility_inventory.service.FacilityInventoryService;
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

@RestController
@RequestMapping({"/api/v1/facility-inventories", "/api/v1/facility-inventory"})
@RequiredArgsConstructor
public class FacilityInventoryController {

    private final FacilityInventoryService facilityInventoryService;

    @PostMapping
    public ResponseEntity<ApiResponse<FacilityInventoryResponse>> create(@Valid @RequestBody FacilityInventoryRequest request) {
        FacilityInventoryResponse response = facilityInventoryService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Facility inventory created successfully", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<FacilityInventoryResponse>>> findAll(
            @ParameterObject @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(ApiResponse.ok("Facility inventories retrieved successfully", facilityInventoryService.findAll(pageable)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<FacilityInventoryResponse>> findById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Facility inventory retrieved successfully", facilityInventoryService.findById(id)));
    }

    @GetMapping("/facility/{facilityId}")
    public ResponseEntity<ApiResponse<List<FacilityInventoryResponse>>> findByFacilityId(@PathVariable Long facilityId) {
        return ResponseEntity.ok(ApiResponse.ok("Facility inventories retrieved successfully", facilityInventoryService.findByFacilityId(facilityId)));
    }

    @GetMapping("/variant/{productVariantId}")
    public ResponseEntity<ApiResponse<List<FacilityInventoryResponse>>> findByProductVariantId(@PathVariable Long productVariantId) {
        return ResponseEntity.ok(ApiResponse.ok("Facility inventories retrieved successfully", facilityInventoryService.findByProductVariantId(productVariantId)));
    }

    @RequestMapping(value = "/{id}", method = {RequestMethod.PATCH, RequestMethod.PUT})
    public ResponseEntity<ApiResponse<FacilityInventoryResponse>> update(@PathVariable Long id, @Valid @RequestBody FacilityInventoryRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Facility inventory updated successfully", facilityInventoryService.update(id, request)));
    }

    @PostMapping("/{id}/reserve")
    public ResponseEntity<ApiResponse<FacilityInventoryResponse>> reserve(@PathVariable Long id, @Valid @RequestBody StockRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Stock reserved successfully", facilityInventoryService.reserve(id, request)));
    }

    @PostMapping("/{id}/release")
    public ResponseEntity<ApiResponse<FacilityInventoryResponse>> release(@PathVariable Long id, @Valid @RequestBody StockRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Stock released successfully", facilityInventoryService.release(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<FacilityInventoryResponse>> softDelete(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Facility inventory soft deleted successfully", facilityInventoryService.softDeleteById(id)));
    }

    @DeleteMapping("/{id}/permanent")
    public ResponseEntity<ApiResponse<Void>> permanentDelete(@PathVariable Long id) {
        facilityInventoryService.permanentDeleteById(id);
        return ResponseEntity.ok(ApiResponse.ok("Facility inventory permanently deleted successfully"));
    }
}
