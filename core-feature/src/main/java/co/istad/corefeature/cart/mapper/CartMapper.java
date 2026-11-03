package co.istad.corefeature.cart.mapper;

import co.istad.corefeature.cart.dto.CartItemResponse;
import co.istad.corefeature.cart.dto.CartRequest;
import co.istad.corefeature.cart.dto.CartResponse;
import co.istad.corefeature.cart.entity.CartEntity;
import co.istad.corefeature.cart.entity.CartItemEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CartMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "items", ignore = true)
    CartEntity toEntity(CartRequest request);

    CartResponse toResponse(CartEntity entity);

    @Mapping(target = "productVariantId", source = "productVariant.id")
    CartItemResponse toItemResponse(CartItemEntity item);
}
