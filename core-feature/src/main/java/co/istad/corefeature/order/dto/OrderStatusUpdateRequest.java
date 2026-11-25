package co.istad.corefeature.order.dto;

import co.istad.corefeature.common.enums.OrderStatus;
import co.istad.corefeature.common.enums.PaymentStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record OrderStatusUpdateRequest(
        @NotNull(message = "Order status is required")
        OrderStatus status,

        String rejectionReason,

        PaymentStatus paymentStatus
) {
}
