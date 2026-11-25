package co.istad.corefeature.order.service;

import co.istad.corefeature.order.dto.OrderRequest;
import co.istad.corefeature.order.dto.OrderResponse;
import co.istad.corefeature.order.dto.OrderStatusUpdateRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface OrderService {

    OrderResponse create(OrderRequest request);

    Page<OrderResponse> findAll(Pageable pageable);

    OrderResponse findById(UUID id);

    OrderResponse findByOrderNumber(String orderNumber);

    Page<OrderResponse> findByCustomerId(String customerId, Pageable pageable);

    OrderResponse updateStatus(UUID id, OrderStatusUpdateRequest request);

    OrderResponse softDeleteById(UUID id);

    void permanentDeleteById(UUID id);
}
