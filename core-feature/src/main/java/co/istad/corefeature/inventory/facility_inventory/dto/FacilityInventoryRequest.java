package co.istad.corefeature.inventory.facility_inventory.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record FacilityInventoryRequest(
        @NotNull(message = "Facility ID is required")
        Long facilityId,

        @NotNull(message = "Product variant ID is required")
        Long productVariantId,

        Integer quantityOnHand,

        Integer reservedQuantity
) {
}
