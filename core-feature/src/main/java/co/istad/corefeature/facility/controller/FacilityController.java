package co.istad.corefeature.facility.controller;

import co.istad.corefeature.common.response.ApiResponse;
import co.istad.corefeature.facility.dto.FacilityRequest;
import co.istad.corefeature.facility.dto.FacilityResponse;
import co.istad.corefeature.facility.service.FacilityService;
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
@RequestMapping({"/api/v1/facilities", "/api/v1/facility"})
@RequiredArgsConstructor
public class FacilityController {

    private final FacilityService facilityService;

    @PostMapping
    public ResponseEntity<ApiResponse<FacilityResponse>> create(@Valid @RequestBody FacilityRequest request) {
        FacilityResponse response = facilityService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Facility created successfully", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<FacilityResponse>>> findAll(
            @ParameterObject @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(ApiResponse.ok("Facilities retrieved successfully", facilityService.findAll(pageable)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<FacilityResponse>> findById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Facility retrieved successfully", facilityService.findById(id)));
    }

    @RequestMapping(value = "/{id}", method = {RequestMethod.PATCH, RequestMethod.PUT})
    public ResponseEntity<ApiResponse<FacilityResponse>> update(@PathVariable Long id, @Valid @RequestBody FacilityRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Facility updated successfully", facilityService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<FacilityResponse>> softDelete(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Facility soft deleted successfully", facilityService.softDeleteById(id)));
    }

    @DeleteMapping("/{id}/permanent")
    public ResponseEntity<ApiResponse<Void>> permanentDelete(@PathVariable Long id) {
        facilityService.permanentDeleteById(id);
        return ResponseEntity.ok(ApiResponse.ok("Facility permanently deleted successfully"));
    }
}
