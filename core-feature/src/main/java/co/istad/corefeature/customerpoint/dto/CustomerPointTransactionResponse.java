package co.istad.corefeature.customerpoint.dto;

import co.istad.corefeature.common.enums.PointTransactionType;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record CustomerPointTransactionResponse(
        Long id,
        Long customerPointId,
        String customerId,
        PointTransactionType transactionType,
        Integer points,
        Integer balanceAfter,
        String referenceId,
        String description,
        LocalDateTime createdAt
) {
}
