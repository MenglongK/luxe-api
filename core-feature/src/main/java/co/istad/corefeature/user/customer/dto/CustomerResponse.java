package co.istad.corefeature.user.customer.dto;

import co.istad.corefeature.common.enums.CustomerTier;
import co.istad.corefeature.common.enums.Gender;
import co.istad.corefeature.facility.dto.AddressResponse;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record CustomerResponse(
        UUID id,
        String customerCode,
        String name,
        String email,
        String phone,
        String avatarUrl,
        CustomerTier tier,
        Gender gender,
        AddressResponse primaryAddress,
        String bespokeNotes,
        Boolean isActive,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
