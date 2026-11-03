package co.istad.corefeature.facility.dto;

import lombok.Builder;

@Builder
public record AddressResponse(
        String addressLine1,
        String addressLine2,
        String city,
        String province,
        String postalCode,
        String country
) {
}
