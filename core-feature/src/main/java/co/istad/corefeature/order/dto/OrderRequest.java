package co.istad.corefeature.order.dto;

import co.istad.corefeature.common.enums.OrderType;
import co.istad.corefeature.common.enums.PaymentMethod;
import co.istad.corefeature.facility.dto.AddressRequest;
import co.istad.corefeature.order.order_item.dto.OrderItemRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Builder
public record OrderRequest(
        @NotBlank(message = "Customer ID is required")
        String customerId,

        @NotBlank(message = "Customer name is required")
        String customerName,

        @NotBlank(message = "Customer email is required")
        @Email(message = "Customer email must be valid")
        String customerEmail,

        String customerPhone,

        Long facilityId,

        @NotNull(message = "Order type is required")
        OrderType orderType,

        @NotNull(message = "Payment method is required")
        PaymentMethod paymentMethod,

        @Valid
        AddressRequest shippingAddress,

        UUID cartId,

        BigDecimal shippingFee,

        BigDecimal discountAmount,

        @NotEmpty(message = "Order must contain at least one item")
        List<@Valid OrderItemRequest> items
) {
}
