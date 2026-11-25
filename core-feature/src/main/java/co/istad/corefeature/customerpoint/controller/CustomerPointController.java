package co.istad.corefeature.customerpoint.controller;

import co.istad.corefeature.common.response.ApiResponse;
import co.istad.corefeature.customerpoint.dto.CustomerPointRequest;
import co.istad.corefeature.customerpoint.dto.CustomerPointResponse;
import co.istad.corefeature.customerpoint.dto.CustomerPointTransactionResponse;
import co.istad.corefeature.customerpoint.dto.PointsRequest;
import co.istad.corefeature.customerpoint.service.CustomerPointService;
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
@RequestMapping({"/api/v1/customer-points", "/api/v1/customer-point"})
@RequiredArgsConstructor
@Tag(name = "Customer Point Controller", description = "Endpoints for managing luxury patron points, reward balances, and point transaction ledgers")
public class CustomerPointController {

    private final CustomerPointService customerPointService;

    @PostMapping
    @Operation(summary = "Create customer points", description = "Initializes a loyalty point account for a patron")
    public ResponseEntity<ApiResponse<CustomerPointResponse>> create(@Valid @RequestBody CustomerPointRequest request) {
        CustomerPointResponse response = customerPointService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Customer points created successfully", response));
    }

    @GetMapping
    @Operation(summary = "Get paginated customer points", description = "Retrieves active patron point balances")
    public ResponseEntity<ApiResponse<Page<CustomerPointResponse>>> findAll(
            @ParameterObject @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(ApiResponse.ok("Customer points retrieved successfully", customerPointService.findAll(pageable)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get customer points by ID", description = "Retrieves patron points record by primary ID")
    public ResponseEntity<ApiResponse<CustomerPointResponse>> findById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Customer points retrieved successfully", customerPointService.findById(id)));
    }

    @GetMapping("/customer/{customerId}")
    @Operation(summary = "Get customer points by customer ID", description = "Retrieves patron points record by unique customer tracking code")
    public ResponseEntity<ApiResponse<CustomerPointResponse>> findByCustomerId(@PathVariable String customerId) {
        return ResponseEntity.ok(ApiResponse.ok("Customer points retrieved successfully", customerPointService.findByCustomerId(customerId)));
    }

    @GetMapping("/{id}/transactions")
    @Operation(summary = "Get point transactions by point ID", description = "Retrieves paginated audit trail of point earnings and redemptions")
    public ResponseEntity<ApiResponse<Page<CustomerPointTransactionResponse>>> findTransactionsByPointId(
            @PathVariable Long id,
            @ParameterObject @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(ApiResponse.ok("Point transactions retrieved successfully", customerPointService.findTransactionsByPointId(id, pageable)));
    }

    @GetMapping("/customer/{customerId}/transactions")
    @Operation(summary = "Get point transactions by customer ID", description = "Retrieves paginated audit trail of point earnings and redemptions by customer code")
    public ResponseEntity<ApiResponse<Page<CustomerPointTransactionResponse>>> findTransactionsByCustomerId(
            @PathVariable String customerId,
            @ParameterObject @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(ApiResponse.ok("Point transactions retrieved successfully", customerPointService.findTransactionsByCustomerId(customerId, pageable)));
    }

    @RequestMapping(value = "/{id}", method = {RequestMethod.PATCH, RequestMethod.PUT})
    @Operation(summary = "Update customer points", description = "Manually updates patron points balance")
    public ResponseEntity<ApiResponse<CustomerPointResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody CustomerPointRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.ok("Customer points updated successfully", customerPointService.update(id, request)));
    }

    @PostMapping("/{id}/earn")
    @Operation(summary = "Earn customer points", description = "Credits points to a patron account from purchase or activity")
    public ResponseEntity<ApiResponse<CustomerPointResponse>> earn(
            @PathVariable Long id,
            @Valid @RequestBody PointsRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.ok("Points earned successfully", customerPointService.earn(id, request)));
    }

    @PostMapping("/{id}/redeem")
    @Operation(summary = "Redeem customer points", description = "Debits points from a patron account for rewards or discounts")
    public ResponseEntity<ApiResponse<CustomerPointResponse>> redeem(
            @PathVariable Long id,
            @Valid @RequestBody PointsRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.ok("Points redeemed successfully", customerPointService.redeem(id, request)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Soft delete customer points", description = "Deactivates patron point account")
    public ResponseEntity<ApiResponse<CustomerPointResponse>> softDelete(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Customer points soft deleted successfully", customerPointService.softDeleteById(id)));
    }

    @DeleteMapping("/{id}/permanent")
    @Operation(summary = "Permanently delete customer points", description = "Hard deletes patron point record from database")
    public ResponseEntity<ApiResponse<Void>> permanentDelete(@PathVariable Long id) {
        customerPointService.permanentDeleteById(id);
        return ResponseEntity.ok(ApiResponse.ok("Customer points permanently deleted successfully"));
    }
}
