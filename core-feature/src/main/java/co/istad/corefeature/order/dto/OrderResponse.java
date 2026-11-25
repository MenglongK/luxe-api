package co.istad.corefeature.order.dto;

import co.istad.corefeature.common.enums.OrderStatus;
import co.istad.corefeature.common.enums.OrderType;
import co.istad.corefeature.common.enums.PaymentMethod;
import co.istad.corefeature.common.enums.PaymentStatus;
import co.istad.corefeature.facility.dto.AddressResponse;
import co.istad.corefeature.order.order_item.dto.OrderItemResponse;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Builder
public record OrderResponse(
        UUID id,
        String orderNumber,
        String customerId,
        String customerName,
        String customerEmail,
        String customerPhone,
        Long facilityId,
        OrderType orderType,
        OrderStatus status,
        String rejectionReason,
        BigDecimal subtotal,
        BigDecimal shippingFee,
        BigDecimal discountAmount,
        BigDecimal grandTotal,
        PaymentMethod paymentMethod,
        PaymentStatus paymentStatus,
        AddressResponse shippingAddress,
        List<OrderItemResponse> items,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
