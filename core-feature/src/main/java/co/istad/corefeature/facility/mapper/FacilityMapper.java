package co.istad.corefeature.facility.mapper;

import co.istad.corefeature.facility.dto.AddressRequest;
import co.istad.corefeature.facility.dto.AddressResponse;
import co.istad.corefeature.facility.dto.FacilityRequest;
import co.istad.corefeature.facility.dto.FacilityResponse;
import co.istad.corefeature.facility.entity.Address;
import co.istad.corefeature.facility.entity.FacilityEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface FacilityMapper {

    @Mapping(target = "id", ignore = true)
    FacilityEntity toEntity(FacilityRequest request);

    FacilityResponse toResponse(FacilityEntity entity);

    @Mapping(target = "id", ignore = true)
    void updateEntityFromRequest(FacilityRequest request, @MappingTarget FacilityEntity entity);

    Address toAddress(AddressRequest request);

    AddressResponse toAddressResponse(Address entity);

    void updateAddressFromRequest(AddressRequest request, @MappingTarget Address entity);
}
