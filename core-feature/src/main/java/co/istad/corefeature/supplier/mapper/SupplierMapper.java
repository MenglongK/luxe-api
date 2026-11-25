package co.istad.corefeature.supplier.mapper;

import co.istad.corefeature.supplier.dto.SupplierRequest;
import co.istad.corefeature.supplier.dto.SupplierResponse;
import co.istad.corefeature.supplier.dto.SupplierUpdateRequest;
import co.istad.corefeature.supplier.entity.SupplierEntity;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface SupplierMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    SupplierEntity toEntity(SupplierRequest request);

    SupplierResponse toResponse(SupplierEntity entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "code", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromDto(SupplierUpdateRequest request, @MappingTarget SupplierEntity entity);
}
