package co.istad.corefeature.customerpoint.controller;

import co.istad.corefeature.common.enums.CustomerTier;
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
@RequestMapping({"/api/v1/customer-points", "/api/v1/customer-point", "/api/v1/memberships", "/api/v1/membership"})
@RequiredArgsConstructor
@Tag(name = "Membership Point Controller", description = "Endpoints for managing patron membership numbers, tier progression, point balances, and point transaction ledgers")
public class CustomerPointController {

    private final CustomerPointService customerPointService;

    @PostMapping
    @Operation(summary = "Create membership point account", description = "Initializes a membership point account and auto-assigns tier based on initial points")
    public ResponseEntity<ApiResponse<CustomerPointResponse>> create(@Valid @RequestBody CustomerPointRequest request) {
        CustomerPointResponse response = customerPointService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Membership points created successfully", response));
    }

    @GetMapping
    @Operation(summary = "Get paginated membership point accounts", description = "Retrieves active patron membership point balances with optional tier and search filters")
    public ResponseEntity<ApiResponse<Page<CustomerPointResponse>>> findAll(
            @RequestParam(required = false) CustomerTier tier,
            @RequestParam(required = false) String search,
            @ParameterObject @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(ApiResponse.ok("Membership points retrieved successfully", customerPointService.findAll(tier, search, pageable)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get membership points by ID", description = "Retrieves patron membership points record by primary ID")
    public ResponseEntity<ApiResponse<CustomerPointResponse>> findById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Membership points retrieved successfully", customerPointService.findById(id)));
    }

    @GetMapping("/customer/{customerId}")
    @Operation(summary = "Get membership points by customer ID", description = "Retrieves patron membership points record by unique customer tracking code")
    public ResponseEntity<ApiResponse<CustomerPointResponse>> findByCustomerId(@PathVariable String customerId) {
        return ResponseEntity.ok(ApiResponse.ok("Membership points retrieved successfully", customerPointService.findByCustomerId(customerId)));
    }

    @GetMapping("/membership/{membershipNumber}")
    @Operation(summary = "Get membership points by membership number", description = "Retrieves patron membership points record by unique membership number")
    public ResponseEntity<ApiResponse<CustomerPointResponse>> findByMembershipNumber(@PathVariable String membershipNumber) {
        return ResponseEntity.ok(ApiResponse.ok("Membership points retrieved successfully", customerPointService.findByMembershipNumber(membershipNumber)));
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
    @Operation(summary = "Update membership points", description = "Manually updates patron points balance and evaluates membership tier upgrade")
    public ResponseEntity<ApiResponse<CustomerPointResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody CustomerPointRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.ok("Membership points updated successfully", customerPointService.update(id, request)));
    }

    @PostMapping("/{id}/earn")
    @Operation(summary = "Earn membership points", description = "Credits points to a patron account and auto-upgrades membership tier if a new threshold is reached")
    public ResponseEntity<ApiResponse<CustomerPointResponse>> earn(
            @PathVariable Long id,
            @Valid @RequestBody PointsRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.ok("Membership points earned successfully", customerPointService.earn(id, request)));
    }

    @PostMapping("/customer/{customerId}/earn")
    @Operation(summary = "Earn membership points by customer ID", description = "Credits points by customer code and auto-upgrades membership tier if a new threshold is reached")
    public ResponseEntity<ApiResponse<CustomerPointResponse>> earnByCustomerId(
            @PathVariable String customerId,
            @Valid @RequestBody PointsRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.ok("Membership points earned successfully", customerPointService.earnByCustomerId(customerId, request)));
    }

    @PostMapping("/{id}/redeem")
    @Operation(summary = "Redeem membership points", description = "Debits spendable points from a patron account while preserving earned membership tier")
    public ResponseEntity<ApiResponse<CustomerPointResponse>> redeem(
            @PathVariable Long id,
            @Valid @RequestBody PointsRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.ok("Membership points redeemed successfully", customerPointService.redeem(id, request)));
    }

    @PostMapping("/customer/{customerId}/redeem")
    @Operation(summary = "Redeem membership points by customer ID", description = "Debits spendable points by customer code while preserving earned membership tier")
    public ResponseEntity<ApiResponse<CustomerPointResponse>> redeemByCustomerId(
            @PathVariable String customerId,
            @Valid @RequestBody PointsRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.ok("Membership points redeemed successfully", customerPointService.redeemByCustomerId(customerId, request)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Soft delete membership points", description = "Deactivates patron membership point account")
    public ResponseEntity<ApiResponse<CustomerPointResponse>> softDelete(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Membership points soft deleted successfully", customerPointService.softDeleteById(id)));
    }

    @DeleteMapping("/{id}/permanent")
    @Operation(summary = "Permanently delete membership points", description = "Hard deletes patron membership point record from database")
    public ResponseEntity<ApiResponse<Void>> permanentDelete(@PathVariable Long id) {
        customerPointService.permanentDeleteById(id);
        return ResponseEntity.ok(ApiResponse.ok("Membership points permanently deleted successfully"));
    }
}
