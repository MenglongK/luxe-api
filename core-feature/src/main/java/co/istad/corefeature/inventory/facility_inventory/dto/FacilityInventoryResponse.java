package co.istad.corefeature.inventory.facility_inventory.dto;

import lombok.Builder;

@Builder
public record FacilityInventoryResponse(
        Long id,
        Long facilityId,
        Long productVariantId,
        Integer quantityOnHand,
        Integer reservedQuantity,
        Boolean isActive
) {
}
