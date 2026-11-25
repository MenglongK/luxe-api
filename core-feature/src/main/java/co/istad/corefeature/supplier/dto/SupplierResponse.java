package co.istad.corefeature.supplier.dto;

import co.istad.corefeature.common.enums.PaymentTerms;
import co.istad.corefeature.common.enums.SupplierCategory;
import co.istad.corefeature.common.enums.SupplierStatus;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record SupplierResponse(
        Long id,
        String code,
        String name,
        SupplierCategory category,
        String contactPerson,
        String email,
        String phone,
        String address,
        Integer rating,
        Integer leadTimeDays,
        PaymentTerms paymentTerms,
        SupplierStatus status,
        Boolean isActive,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
