package co.istad.corefeature.wallet.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record WalletPaymentRequest(
        @NotNull(message = "Payment amount is required")
        @DecimalMin(value = "0.01", message = "Payment amount must be at least 0.01")
        BigDecimal amount,

        String orderId,

        String description
) {
}
