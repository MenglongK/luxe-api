package co.istad.corefeature.inventory.facility_inventory.mapper;

import co.istad.corefeature.facility.entity.FacilityEntity;
import co.istad.corefeature.inventory.facility_inventory.dto.FacilityInventoryRequest;
import co.istad.corefeature.inventory.facility_inventory.dto.FacilityInventoryResponse;
import co.istad.corefeature.inventory.facility_inventory.entity.FacilityInventoryEntity;
import co.istad.corefeature.product.entity.ProductVariantEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface FacilityInventoryMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    @Mapping(target = "facility", source = "facility")
    @Mapping(target = "productVariant", source = "productVariant")
    @Mapping(target = "quantityOnHand", source = "request.quantityOnHand")
    @Mapping(target = "reservedQuantity", source = "request.reservedQuantity")
    FacilityInventoryEntity toEntity(
            FacilityInventoryRequest request,
            FacilityEntity facility,
            ProductVariantEntity productVariant
    );

    @Mapping(target = "facilityId", source = "facility.id")
    @Mapping(target = "productVariantId", source = "productVariant.id")
    FacilityInventoryResponse toResponse(FacilityInventoryEntity entity);
}
