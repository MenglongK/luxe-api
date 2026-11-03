package co.istad.corefeature.brand.mapper;

import co.istad.corefeature.brand.dto.BrandRequest;
import co.istad.corefeature.brand.dto.BrandResponse;
import co.istad.corefeature.brand.entity.BrandEntity;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface BrandMapper {
    BrandEntity toEntity(BrandRequest brandRequest);

    BrandResponse toResponse(BrandEntity brandEntity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDto(BrandRequest request, @MappingTarget BrandEntity entity);
}
