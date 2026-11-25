package co.istad.corefeature.wishlist.controller;

import co.istad.corefeature.cart.dto.CartResponse;
import co.istad.corefeature.common.response.ApiResponse;
import co.istad.corefeature.wishlist.dto.WishlistItemRequest;
import co.istad.corefeature.wishlist.dto.WishlistRequest;
import co.istad.corefeature.wishlist.dto.WishlistResponse;
import co.istad.corefeature.wishlist.service.WishlistService;
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
@RequestMapping({"/api/v1/wishlists", "/api/v1/wishlist"})
@RequiredArgsConstructor
@Tag(name = "Wishlist Controller", description = "Endpoints for managing customer wishlists and saved luxury items")
public class WishlistController {

    private final WishlistService wishlistService;

    @PostMapping
    @Operation(summary = "Create customer wishlist", description = "Initializes a new wishlist container for a customer")
    public ResponseEntity<ApiResponse<WishlistResponse>> create(@Valid @RequestBody WishlistRequest request) {
        WishlistResponse response = wishlistService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Wishlist created successfully", response));
    }

    @GetMapping
    @Operation(summary = "Get paginated wishlists", description = "Retrieves active wishlists with pagination and sorting")
    public ResponseEntity<ApiResponse<Page<WishlistResponse>>> findAll(
            @ParameterObject @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(ApiResponse.ok("Wishlists retrieved successfully", wishlistService.findAll(pageable)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get wishlist by ID", description = "Retrieves active wishlist by UUID")
    public ResponseEntity<ApiResponse<WishlistResponse>> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok("Wishlist retrieved successfully", wishlistService.findById(id)));
    }

    @GetMapping("/customer/{customerId}")
    @Operation(summary = "Get wishlist by customer ID", description = "Retrieves active wishlist belonging to a specific customer")
    public ResponseEntity<ApiResponse<WishlistResponse>> findByCustomerId(@PathVariable String customerId) {
        return ResponseEntity.ok(ApiResponse.ok("Customer wishlist retrieved successfully", wishlistService.findByCustomerId(customerId)));
    }

    @PostMapping("/{id}/items")
    @Operation(summary = "Add item to wishlist by ID", description = "Appends a product variant to an existing wishlist by its UUID")
    public ResponseEntity<ApiResponse<WishlistResponse>> addItem(
            @PathVariable UUID id,
            @Valid @RequestBody WishlistItemRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.ok("Item added to wishlist successfully", wishlistService.addItem(id, request)));
    }

    @PostMapping("/customer/{customerId}/items")
    @Operation(summary = "Add item to customer wishlist", description = "Appends a product variant directly by customer ID, creating wishlist if not yet existing")
    public ResponseEntity<ApiResponse<WishlistResponse>> addItemByCustomer(
            @PathVariable String customerId,
            @Valid @RequestBody WishlistItemRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.ok("Item added to customer wishlist successfully", wishlistService.addItemByCustomerId(customerId, request)));
    }

    @DeleteMapping("/{id}/items/{itemId}")
    @Operation(summary = "Remove item from wishlist by item ID", description = "Removes a specific item entry from a wishlist")
    public ResponseEntity<ApiResponse<WishlistResponse>> removeItem(
            @PathVariable UUID id,
            @PathVariable Long itemId
    ) {
        return ResponseEntity.ok(ApiResponse.ok("Item removed from wishlist successfully", wishlistService.removeItem(id, itemId)));
    }

    @DeleteMapping("/customer/{customerId}/items/{variantId}")
    @Operation(summary = "Remove item by variant ID", description = "Removes a specific product variant from a customer's active wishlist")
    public ResponseEntity<ApiResponse<WishlistResponse>> removeItemByVariant(
            @PathVariable String customerId,
            @PathVariable Long variantId
    ) {
        return ResponseEntity.ok(ApiResponse.ok("Product variant removed from wishlist successfully", wishlistService.removeItemByVariantId(customerId, variantId)));
    }

    @PostMapping("/customer/{customerId}/move-to-cart/{variantId}")
    @Operation(summary = "Move item from wishlist to cart", description = "Atomically transfers a wishlisted product variant into the customer's active shopping cart")
    public ResponseEntity<ApiResponse<CartResponse>> moveToCart(
            @PathVariable String customerId,
            @PathVariable Long variantId
    ) {
        return ResponseEntity.ok(ApiResponse.ok("Item moved to cart successfully", wishlistService.moveToCart(customerId, variantId)));
    }

    @DeleteMapping("/{id}/items")
    @Operation(summary = "Clear wishlist items", description = "Empties all items from a wishlist by UUID")
    public ResponseEntity<ApiResponse<WishlistResponse>> clearItems(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok("Wishlist items cleared successfully", wishlistService.clearItems(id)));
    }

    @DeleteMapping("/customer/{customerId}/clear")
    @Operation(summary = "Clear customer wishlist", description = "Empties all items from a customer's active wishlist")
    public ResponseEntity<ApiResponse<WishlistResponse>> clearCustomerWishlist(@PathVariable String customerId) {
        return ResponseEntity.ok(ApiResponse.ok("Customer wishlist cleared successfully", wishlistService.clearItemsByCustomerId(customerId)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Soft delete wishlist", description = "Deactivates a wishlist")
    public ResponseEntity<ApiResponse<WishlistResponse>> softDelete(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok("Wishlist soft deleted successfully", wishlistService.softDeleteById(id)));
    }

    @DeleteMapping("/{id}/permanent")
    @Operation(summary = "Permanently delete wishlist", description = "Hard deletes a wishlist from the database")
    public ResponseEntity<ApiResponse<Void>> permanentDelete(@PathVariable UUID id) {
        wishlistService.permanentDeleteById(id);
        return ResponseEntity.ok(ApiResponse.ok("Wishlist permanently deleted successfully"));
    }
}
