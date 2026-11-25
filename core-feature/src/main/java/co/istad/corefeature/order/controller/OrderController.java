package co.istad.corefeature.order.controller;

import co.istad.corefeature.common.response.ApiResponse;
import co.istad.corefeature.order.dto.OrderRequest;
import co.istad.corefeature.order.dto.OrderResponse;
import co.istad.corefeature.order.dto.OrderStatusUpdateRequest;
import co.istad.corefeature.order.service.OrderService;
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
@RequestMapping({"/api/v1/orders", "/api/v1/order"})
@RequiredArgsConstructor
@Tag(name = "Order Controller", description = "Endpoints for managing luxury customer orders and fulfillments")
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @Operation(summary = "Place a new order", description = "Places an order, validates and deducts inventory, computes totals, and optionally clears the checkout cart")
    public ResponseEntity<ApiResponse<OrderResponse>> create(@Valid @RequestBody OrderRequest request) {
        OrderResponse response = orderService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Order placed successfully", response));
    }

    @GetMapping
    @Operation(summary = "Get paginated orders", description = "Retrieves active orders with pagination and sorting")
    public ResponseEntity<ApiResponse<Page<OrderResponse>>> findAll(
            @ParameterObject @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(ApiResponse.ok("Orders retrieved successfully", orderService.findAll(pageable)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get order by ID", description = "Retrieves an active order by its UUID")
    public ResponseEntity<ApiResponse<OrderResponse>> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok("Order retrieved successfully", orderService.findById(id)));
    }

    @GetMapping("/order-number/{orderNumber}")
    @Operation(summary = "Get order by order number", description = "Retrieves an active order by its unique tracking order number")
    public ResponseEntity<ApiResponse<OrderResponse>> findByOrderNumber(@PathVariable String orderNumber) {
        return ResponseEntity.ok(ApiResponse.ok("Order retrieved successfully", orderService.findByOrderNumber(orderNumber)));
    }

    @GetMapping("/customer/{customerId}")
    @Operation(summary = "Get orders by customer ID", description = "Retrieves paginated orders belonging to a specific customer")
    public ResponseEntity<ApiResponse<Page<OrderResponse>>> findByCustomerId(
            @PathVariable String customerId,
            @ParameterObject @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(ApiResponse.ok("Customer orders retrieved successfully", orderService.findByCustomerId(customerId, pageable)));
    }

    @RequestMapping(value = "/{id}/status", method = {RequestMethod.PATCH, RequestMethod.PUT})
    @Operation(summary = "Update order status", description = "Updates order progress or cancels/rejects with inventory restoration")
    public ResponseEntity<ApiResponse<OrderResponse>> updateStatus(
            @PathVariable UUID id,
            @Valid @RequestBody OrderStatusUpdateRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.ok("Order status updated successfully", orderService.updateStatus(id, request)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Soft delete order", description = "Marks an order as inactive and releases reserved inventory if still in progress")
    public ResponseEntity<ApiResponse<OrderResponse>> softDelete(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok("Order soft deleted successfully", orderService.softDeleteById(id)));
    }

    @DeleteMapping("/{id}/permanent")
    @Operation(summary = "Permanently delete order", description = "Hard deletes an order from the database")
    public ResponseEntity<ApiResponse<Void>> permanentDelete(@PathVariable UUID id) {
        orderService.permanentDeleteById(id);
        return ResponseEntity.ok(ApiResponse.ok("Order permanently deleted successfully"));
    }
}
