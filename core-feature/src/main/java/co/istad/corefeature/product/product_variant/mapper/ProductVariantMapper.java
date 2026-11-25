package co.istad.corefeature.product.product_variant.mapper;

import co.istad.corefeature.product.product_variant.dto.ProductVariantRequest;
import co.istad.corefeature.product.product_variant.dto.ProductVariantResponse;
import co.istad.corefeature.product.product_variant.entity.ProductVariantEntity;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface ProductVariantMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "skuId", ignore = true)
    @Mapping(target = "product", ignore = true)
    ProductVariantEntity toEntity(ProductVariantRequest request);

    @Mapping(source = "product.id", target = "productId")
    @Mapping(source = "product.name", target = "productName")
    ProductVariantResponse toResponse(ProductVariantEntity entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "skuId", ignore = true)
    @Mapping(target = "product", ignore = true)
    void updateEntityFromDto(ProductVariantRequest request, @MappingTarget ProductVariantEntity entity);
}
