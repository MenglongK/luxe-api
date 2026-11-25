package co.istad.corefeature.wallet.dto;

import co.istad.corefeature.common.enums.WalletStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record WalletStatusUpdateRequest(
        @NotNull(message = "Wallet status is required")
        WalletStatus status
) {
}
