package co.istad.corefeature.facility.dto;

import co.istad.corefeature.common.enums.FacilityType;
import lombok.Builder;

@Builder
public record FacilityResponse(
        Long id,
        String code,
        String name,
        FacilityType type,
        AddressResponse address,
        Boolean isActive
) {
}
