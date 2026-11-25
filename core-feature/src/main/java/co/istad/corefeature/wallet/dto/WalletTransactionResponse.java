package co.istad.corefeature.wallet.dto;

import co.istad.corefeature.common.enums.WalletTransactionType;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record WalletTransactionResponse(
        UUID id,
        UUID walletId,
        String customerId,
        WalletTransactionType transactionType,
        BigDecimal amount,
        BigDecimal balanceAfter,
        String referenceId,
        String description,
        LocalDateTime createdAt
) {
}
