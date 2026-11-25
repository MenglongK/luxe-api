package co.istad.corefeature.product.mapper;

import co.istad.corefeature.product.dto.ProductRequest;
import co.istad.corefeature.product.dto.ProductResponse;
import co.istad.corefeature.product.entity.ProductEntity;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    ProductEntity toEntity(ProductRequest productRequest);

    ProductResponse toResponse(ProductEntity productEntity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDto(ProductRequest request, @MappingTarget ProductEntity entity);
}
