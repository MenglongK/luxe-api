package co.istad.corefeature.coupon.controller;

import co.istad.corefeature.common.enums.CouponStatus;
import co.istad.corefeature.common.enums.DiscountType;
import co.istad.corefeature.common.response.ApiResponse;
import co.istad.corefeature.coupon.dto.*;
import co.istad.corefeature.coupon.service.CouponService;
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
@RequestMapping({"/api/v1/coupons", "/api/v1/coupon"})
@RequiredArgsConstructor
@Tag(name = "Coupon Controller", description = "Endpoints for managing promotional campaigns, discount vouchers, and checkout coupon validation")
public class CouponController {

    private final CouponService couponService;

    @PostMapping
    @Operation(summary = "Create coupon", description = "Creates a new promotional discount coupon or voucher code")
    public ResponseEntity<ApiResponse<CouponResponse>> create(@Valid @RequestBody CouponRequest request) {
        CouponResponse response = couponService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Coupon created successfully", response));
    }

    @GetMapping
    @Operation(summary = "Get paginated coupons", description = "Retrieves active coupons with optional status filter, discount type filter, and search")
    public ResponseEntity<ApiResponse<Page<CouponResponse>>> findAll(
            @RequestParam(required = false) CouponStatus status,
            @RequestParam(required = false) DiscountType discountType,
            @RequestParam(required = false) String search,
            @ParameterObject @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(ApiResponse.ok("Coupons retrieved successfully", couponService.findAll(status, discountType, search, pageable)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get coupon by ID", description = "Retrieves coupon details by UUID")
    public ResponseEntity<ApiResponse<CouponResponse>> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok("Coupon retrieved successfully", couponService.findById(id)));
    }

    @GetMapping("/code/{code}")
    @Operation(summary = "Get coupon by code", description = "Retrieves coupon details by promotional code")
    public ResponseEntity<ApiResponse<CouponResponse>> findByCode(@PathVariable String code) {
        return ResponseEntity.ok(ApiResponse.ok("Coupon retrieved successfully", couponService.findByCode(code)));
    }

    @PostMapping("/validate")
    @Operation(summary = "Validate coupon for cart", description = "Validates promo code against order subtotal and computes exact savings")
    public ResponseEntity<ApiResponse<CouponValidationResponse>> validateCoupon(@Valid @RequestBody CouponValidateRequest request) {
        CouponValidationResponse response = couponService.validateCoupon(request);
        return ResponseEntity.ok(ApiResponse.ok("Coupon validation completed", response));
    }

    @PostMapping("/code/{code}/increment")
    @Operation(summary = "Increment coupon usage count", description = "Records a redemption of the coupon upon order completion")
    public ResponseEntity<ApiResponse<CouponResponse>> incrementUsage(@PathVariable String code) {
        return ResponseEntity.ok(ApiResponse.ok("Coupon usage updated successfully", couponService.incrementUsage(code)));
    }

    @RequestMapping(value = "/{id}", method = {RequestMethod.PATCH, RequestMethod.PUT})
    @Operation(summary = "Update coupon", description = "Updates coupon promotional rules, usage limits, or expiry date")
    public ResponseEntity<ApiResponse<CouponResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody CouponUpdateRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.ok("Coupon updated successfully", couponService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Soft delete coupon", description = "Deactivates a coupon")
    public ResponseEntity<ApiResponse<CouponResponse>> softDelete(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok("Coupon soft deleted successfully", couponService.softDeleteById(id)));
    }

    @DeleteMapping("/{id}/permanent")
    @Operation(summary = "Permanently delete coupon", description = "Hard deletes a coupon from the database")
    public ResponseEntity<ApiResponse<Void>> permanentDelete(@PathVariable UUID id) {
        couponService.permanentDeleteById(id);
        return ResponseEntity.ok(ApiResponse.ok("Coupon permanently deleted successfully"));
    }
}
