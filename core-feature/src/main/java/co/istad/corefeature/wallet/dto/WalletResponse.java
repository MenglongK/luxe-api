package co.istad.corefeature.wallet.dto;

import co.istad.corefeature.common.enums.WalletStatus;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record WalletResponse(
        UUID id,
        String walletNumber,
        String customerId,
        BigDecimal balance,
        String currency,
        WalletStatus status,
        Boolean isActive,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
