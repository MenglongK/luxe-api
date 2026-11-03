package co.istad.corefeature.cart.controller;

import co.istad.corefeature.cart.dto.CartItemRequest;
import co.istad.corefeature.cart.dto.CartItemUpdateRequest;
import co.istad.corefeature.cart.dto.CartRequest;
import co.istad.corefeature.cart.dto.CartResponse;
import co.istad.corefeature.cart.service.CartService;
import co.istad.corefeature.common.response.ApiResponse;
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
@RequestMapping("/api/v1/carts")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @PostMapping
    public ResponseEntity<ApiResponse<CartResponse>> create(@Valid @RequestBody CartRequest request) {
        CartResponse response = cartService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Cart created successfully", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<CartResponse>>> findAll(
            @ParameterObject @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(ApiResponse.ok("Carts retrieved successfully", cartService.findAll(pageable)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CartResponse>> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok("Cart retrieved successfully", cartService.findById(id)));
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<ApiResponse<CartResponse>> findByCustomerId(@PathVariable String customerId) {
        return ResponseEntity.ok(ApiResponse.ok("Cart retrieved successfully", cartService.findByCustomerId(customerId)));
    }

    @PostMapping("/{id}/items")
    public ResponseEntity<ApiResponse<CartResponse>> addItem(@PathVariable UUID id, @Valid @RequestBody CartItemRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Item added to cart successfully", cartService.addItem(id, request)));
    }

    @PutMapping("/{id}/items/{itemId}")
    public ResponseEntity<ApiResponse<CartResponse>> updateItem(@PathVariable UUID id, @PathVariable Long itemId, @Valid @RequestBody CartItemUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Cart item updated successfully", cartService.updateItem(id, itemId, request)));
    }

    @DeleteMapping("/{id}/items/{itemId}")
    public ResponseEntity<ApiResponse<CartResponse>> removeItem(@PathVariable UUID id, @PathVariable Long itemId) {
        return ResponseEntity.ok(ApiResponse.ok("Item removed from cart successfully", cartService.removeItem(id, itemId)));
    }

    @DeleteMapping("/{id}/items")
    public ResponseEntity<ApiResponse<CartResponse>> clearItems(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok("Cart items cleared successfully", cartService.clearItems(id)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<CartResponse>> softDelete(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok("Cart soft deleted successfully", cartService.softDeleteById(id)));
    }

    @DeleteMapping("/{id}/permanent")
    public ResponseEntity<ApiResponse<Void>> permanentDelete(@PathVariable UUID id) {
        cartService.permanentDeleteById(id);
        return ResponseEntity.ok(ApiResponse.ok("Cart permanently deleted successfully"));
    }
}
