package co.istad.corefeature.wallet.controller;

import co.istad.corefeature.common.enums.WalletStatus;
import co.istad.corefeature.common.response.ApiResponse;
import co.istad.corefeature.wallet.dto.*;
import co.istad.corefeature.wallet.service.WalletService;
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
@RequestMapping({"/api/v1/wallets", "/api/v1/wallet"})
@RequiredArgsConstructor
@Tag(name = "Loyalty Wallet Controller", description = "Endpoints for managing patron digital wallets, luxury store credits, deposits, and checkout balance payments")
public class WalletController {

    private final WalletService walletService;

    @PostMapping
    @Operation(summary = "Create customer loyalty wallet", description = "Initializes a digital store credit and loyalty wallet for a patron")
    public ResponseEntity<ApiResponse<WalletResponse>> create(@Valid @RequestBody WalletCreateRequest request) {
        WalletResponse response = walletService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Loyalty wallet created successfully", response));
    }

    @GetMapping
    @Operation(summary = "Get paginated loyalty wallets", description = "Retrieves active patron wallets with optional status filter and customer search")
    public ResponseEntity<ApiResponse<Page<WalletResponse>>> findAll(
            @RequestParam(required = false) WalletStatus status,
            @RequestParam(required = false) String search,
            @ParameterObject @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(ApiResponse.ok("Loyalty wallets retrieved successfully", walletService.findAll(status, search, pageable)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get wallet by ID", description = "Retrieves wallet details and balance by UUID")
    public ResponseEntity<ApiResponse<WalletResponse>> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok("Loyalty wallet retrieved successfully", walletService.findById(id)));
    }

    @GetMapping("/customer/{customerId}")
    @Operation(summary = "Get wallet by customer ID", description = "Retrieves active patron wallet by customer code or ID")
    public ResponseEntity<ApiResponse<WalletResponse>> findByCustomerId(@PathVariable String customerId) {
        return ResponseEntity.ok(ApiResponse.ok("Loyalty wallet retrieved successfully", walletService.findByCustomerId(customerId)));
    }

    @GetMapping("/number/{walletNumber}")
    @Operation(summary = "Get wallet by wallet number", description = "Retrieves wallet details by unique wallet tracking number")
    public ResponseEntity<ApiResponse<WalletResponse>> findByWalletNumber(@PathVariable String walletNumber) {
        return ResponseEntity.ok(ApiResponse.ok("Loyalty wallet retrieved successfully", walletService.findByWalletNumber(walletNumber)));
    }

    @PostMapping("/{id}/deposit")
    @Operation(summary = "Deposit store credit into wallet", description = "Adds funds or store credit top-up to patron wallet")
    public ResponseEntity<ApiResponse<WalletResponse>> deposit(
            @PathVariable UUID id,
            @Valid @RequestBody WalletDepositRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.ok("Store credit deposited successfully", walletService.deposit(id, request)));
    }

    @PostMapping("/{id}/pay")
    @Operation(summary = "Pay with wallet balance", description = "Deducts store credit from patron wallet for order checkout")
    public ResponseEntity<ApiResponse<WalletResponse>> pay(
            @PathVariable UUID id,
            @Valid @RequestBody WalletPaymentRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.ok("Wallet payment processed successfully", walletService.pay(id, request)));
    }

    @GetMapping("/{id}/transactions")
    @Operation(summary = "Get wallet transaction history", description = "Retrieves paginated ledger of deposits, refunds, and payments for a wallet")
    public ResponseEntity<ApiResponse<Page<WalletTransactionResponse>>> findTransactionsByWalletId(
            @PathVariable UUID id,
            @ParameterObject @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(ApiResponse.ok("Wallet transactions retrieved successfully", walletService.findTransactionsByWalletId(id, pageable)));
    }

    @GetMapping("/customer/{customerId}/transactions")
    @Operation(summary = "Get customer wallet transactions", description = "Retrieves paginated ledger of transactions for a customer")
    public ResponseEntity<ApiResponse<Page<WalletTransactionResponse>>> findTransactionsByCustomerId(
            @PathVariable String customerId,
            @ParameterObject @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(ApiResponse.ok("Wallet transactions retrieved successfully", walletService.findTransactionsByCustomerId(customerId, pageable)));
    }

    @RequestMapping(value = "/{id}/status", method = {RequestMethod.PATCH, RequestMethod.PUT})
    @Operation(summary = "Update wallet status", description = "Updates wallet state (e.g., ACTIVE, FROZEN, CLOSED)")
    public ResponseEntity<ApiResponse<WalletResponse>> updateStatus(
            @PathVariable UUID id,
            @Valid @RequestBody WalletStatusUpdateRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.ok("Wallet status updated successfully", walletService.updateStatus(id, request)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Soft delete wallet", description = "Deactivates patron wallet and closes account")
    public ResponseEntity<ApiResponse<WalletResponse>> softDelete(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok("Loyalty wallet soft deleted successfully", walletService.softDeleteById(id)));
    }

    @DeleteMapping("/{id}/permanent")
    @Operation(summary = "Permanently delete wallet", description = "Hard deletes wallet record from database")
    public ResponseEntity<ApiResponse<Void>> permanentDelete(@PathVariable UUID id) {
        walletService.permanentDeleteById(id);
        return ResponseEntity.ok(ApiResponse.ok("Loyalty wallet permanently deleted successfully"));
    }
}
