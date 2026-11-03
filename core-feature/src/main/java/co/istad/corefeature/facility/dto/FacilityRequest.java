package co.istad.corefeature.facility.dto;

import co.istad.corefeature.common.enums.FacilityType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record FacilityRequest(
        @NotBlank(message = "Code is required")
        @Size(max = 20, message = "Code must not exceed 20 characters")
        String code,

        @NotBlank(message = "Name is required")
        String name,

        @NotNull(message = "Type is required")
        FacilityType type,

        @Valid
        AddressRequest address,

        Boolean isActive
) {
}
