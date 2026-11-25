package co.istad.corefeature.user.customer.controller;

import co.istad.corefeature.common.enums.CustomerTier;
import co.istad.corefeature.common.response.ApiResponse;
import co.istad.corefeature.user.customer.dto.CustomerRequest;
import co.istad.corefeature.user.customer.dto.CustomerResponse;
import co.istad.corefeature.user.customer.dto.CustomerSummaryResponse;
import co.istad.corefeature.user.customer.dto.CustomerUpdateRequest;
import co.istad.corefeature.user.customer.service.CustomerService;
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

import java.util.UUID;

@RestController
@RequestMapping({"/api/v1/customers", "/api/v1/customer"})
@RequiredArgsConstructor
@Tag(name = "Customer Controller", description = "Endpoints for managing luxury customer profiles, VIP clientele, and patron relations")
public class CustomerController {

    private final CustomerService customerService;

    @PostMapping
    @Operation(summary = "Register customer profile", description = "Creates a new client profile with contact and optional bespoke preferences")
    public ResponseEntity<ApiResponse<CustomerResponse>> create(@Valid @RequestBody CustomerRequest request) {
        CustomerResponse response = customerService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Customer registered successfully", response));
    }

    @GetMapping
    @Operation(summary = "Get paginated customers", description = "Retrieves active customers with optional tier filter, search query, and sorting")
    public ResponseEntity<ApiResponse<Page<CustomerResponse>>> findAll(
            @RequestParam(required = false) CustomerTier tier,
            @RequestParam(required = false) String search,
            @ParameterObject @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(ApiResponse.ok("Customers retrieved successfully", customerService.findAll(tier, search, pageable)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get customer by ID", description = "Retrieves active customer profile by UUID")
    public ResponseEntity<ApiResponse<CustomerResponse>> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok("Customer retrieved successfully", customerService.findById(id)));
    }

    @GetMapping("/code/{customerCode}")
    @Operation(summary = "Get customer by customer code", description = "Retrieves active customer profile by unique customer tracking code")
    public ResponseEntity<ApiResponse<CustomerResponse>> findByCustomerCode(@PathVariable String customerCode) {
        return ResponseEntity.ok(ApiResponse.ok("Customer retrieved successfully", customerService.findByCustomerCode(customerCode)));
    }

    @GetMapping("/by-email/{email}")
    @Operation(summary = "Get customer by email", description = "Retrieves active customer profile by email address")
    public ResponseEntity<ApiResponse<CustomerResponse>> findByEmail(@PathVariable String email) {
        return ResponseEntity.ok(ApiResponse.ok("Customer retrieved successfully", customerService.findByEmail(email)));
    }

    @GetMapping("/{id}/summary")
    @Operation(summary = "Get customer summary & metrics", description = "Retrieves customer profile with aggregated orders, total spend, and loyalty points")
    public ResponseEntity<ApiResponse<CustomerSummaryResponse>> getSummary(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok("Customer summary retrieved successfully", customerService.getSummary(id)));
    }

    @RequestMapping(value = "/{id}", method = {RequestMethod.PATCH, RequestMethod.PUT})
    @Operation(summary = "Update customer profile", description = "Updates customer contact details, VIP tier, or bespoke tailoring notes")
    public ResponseEntity<ApiResponse<CustomerResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody CustomerUpdateRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.ok("Customer updated successfully", customerService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Soft delete customer", description = "Marks a customer profile as inactive")
    public ResponseEntity<ApiResponse<CustomerResponse>> softDelete(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok("Customer soft deleted successfully", customerService.softDeleteById(id)));
    }

    @DeleteMapping("/{id}/permanent")
    @Operation(summary = "Permanently delete customer", description = "Hard deletes customer record from the database")
    public ResponseEntity<ApiResponse<Void>> permanentDelete(@PathVariable UUID id) {
        customerService.permanentDeleteById(id);
        return ResponseEntity.ok(ApiResponse.ok("Customer permanently deleted successfully"));
    }
}
