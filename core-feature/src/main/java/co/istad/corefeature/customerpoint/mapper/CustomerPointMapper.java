package co.istad.corefeature.customerpoint.mapper;

import co.istad.corefeature.customerpoint.dto.CustomerPointRequest;
import co.istad.corefeature.customerpoint.dto.CustomerPointResponse;
import co.istad.corefeature.customerpoint.entity.CustomerPointEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CustomerPointMapper {

    @Mapping(target = "id", ignore = true)
    CustomerPointEntity toEntity(CustomerPointRequest request);

    CustomerPointResponse toResponse(CustomerPointEntity entity);
}
