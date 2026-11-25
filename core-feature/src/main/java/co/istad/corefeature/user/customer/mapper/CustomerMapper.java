package co.istad.corefeature.user.customer.mapper;

import co.istad.corefeature.facility.dto.AddressRequest;
import co.istad.corefeature.facility.dto.AddressResponse;
import co.istad.corefeature.facility.entity.Address;
import co.istad.corefeature.user.customer.dto.CustomerRequest;
import co.istad.corefeature.user.customer.dto.CustomerResponse;
import co.istad.corefeature.user.customer.dto.CustomerUpdateRequest;
import co.istad.corefeature.user.customer.entity.CustomerEntity;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface CustomerMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    CustomerEntity toEntity(CustomerRequest request);

    CustomerResponse toResponse(CustomerEntity entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "customerCode", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromDto(CustomerUpdateRequest request, @MappingTarget CustomerEntity entity);

    Address toAddress(AddressRequest request);

    AddressResponse toAddressResponse(Address address);
}
