package co.istad.corefeature.customerpoint.controller;

import co.istad.corefeature.common.response.ApiResponse;
import co.istad.corefeature.customerpoint.dto.CustomerPointRequest;
import co.istad.corefeature.customerpoint.dto.CustomerPointResponse;
import co.istad.corefeature.customerpoint.dto.PointsRequest;
import co.istad.corefeature.customerpoint.service.CustomerPointService;
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
@RequestMapping("/api/v1/customer-points")
@RequiredArgsConstructor
public class CustomerPointController {

    private final CustomerPointService customerPointService;

    @PostMapping
    public ResponseEntity<ApiResponse<CustomerPointResponse>> create(@Valid @RequestBody CustomerPointRequest request) {
        CustomerPointResponse response = customerPointService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Customer points created successfully", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<CustomerPointResponse>>> findAll(
            @ParameterObject @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(ApiResponse.ok("Customer points retrieved successfully", customerPointService.findAll(pageable)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CustomerPointResponse>> findById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Customer points retrieved successfully", customerPointService.findById(id)));
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<ApiResponse<CustomerPointResponse>> findByCustomerId(@PathVariable String customerId) {
        return ResponseEntity.ok(ApiResponse.ok("Customer points retrieved successfully", customerPointService.findByCustomerId(customerId)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CustomerPointResponse>> update(@PathVariable Long id, @Valid @RequestBody CustomerPointRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Customer points updated successfully", customerPointService.update(id, request)));
    }

    @PostMapping("/{id}/earn")
    public ResponseEntity<ApiResponse<CustomerPointResponse>> earn(@PathVariable Long id, @Valid @RequestBody PointsRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Points earned successfully", customerPointService.earn(id, request)));
    }

    @PostMapping("/{id}/redeem")
    public ResponseEntity<ApiResponse<CustomerPointResponse>> redeem(@PathVariable Long id, @Valid @RequestBody PointsRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Points redeemed successfully", customerPointService.redeem(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<CustomerPointResponse>> softDelete(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Customer points soft deleted successfully", customerPointService.softDeleteById(id)));
    }

    @DeleteMapping("/{id}/permanent")
    public ResponseEntity<ApiResponse<Void>> permanentDelete(@PathVariable Long id) {
        customerPointService.permanentDeleteById(id);
        return ResponseEntity.ok(ApiResponse.ok("Customer points permanently deleted successfully"));
    }
}
