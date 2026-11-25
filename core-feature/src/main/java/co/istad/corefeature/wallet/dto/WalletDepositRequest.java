package co.istad.corefeature.wallet.dto;

import co.istad.corefeature.common.enums.PaymentMethod;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record WalletDepositRequest(
        @NotNull(message = "Deposit amount is required")
        @DecimalMin(value = "0.01", message = "Deposit amount must be at least 0.01")
        BigDecimal amount,

        PaymentMethod paymentMethod,

        String referenceId,

        String description
) {
}
