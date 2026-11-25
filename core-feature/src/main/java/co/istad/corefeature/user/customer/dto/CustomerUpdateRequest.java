package co.istad.corefeature.user.customer.dto;

import co.istad.corefeature.common.enums.CustomerTier;
import co.istad.corefeature.common.enums.Gender;
import co.istad.corefeature.facility.dto.AddressRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record CustomerUpdateRequest(
        @Size(max = 120, message = "Name cannot exceed 120 characters")
        String name,

        @Email(message = "Must be a valid email format")
        @Size(max = 150, message = "Email cannot exceed 150 characters")
        String email,

        @Pattern(regexp = "^\\+?[0-9\\s-]{8,25}$", message = "Phone number must be valid format (8-25 characters)")
        String phone,

        String avatarUrl,

        CustomerTier tier,

        Gender gender,

        @Valid
        AddressRequest primaryAddress,

        String bespokeNotes,

        Boolean isActive
) {
}
