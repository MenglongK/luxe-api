package co.istad.corefeature.wallet.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record WalletCreateRequest(
        @NotBlank(message = "Customer ID is required")
        String customerId,

        @PositiveOrZero(message = "Initial balance cannot be negative")
        BigDecimal initialBalance,

        String currency
) {
}
