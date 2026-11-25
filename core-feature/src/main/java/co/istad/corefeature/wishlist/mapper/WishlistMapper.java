package co.istad.corefeature.wishlist.mapper;

import co.istad.corefeature.wishlist.dto.WishlistItemResponse;
import co.istad.corefeature.wishlist.dto.WishlistRequest;
import co.istad.corefeature.wishlist.dto.WishlistResponse;
import co.istad.corefeature.wishlist.entity.WishlistEntity;
import co.istad.corefeature.wishlist.entity.WishlistItemEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface WishlistMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "items", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    WishlistEntity toEntity(WishlistRequest request);

    @Mapping(target = "totalItems", expression = "java(entity.getItems() != null ? entity.getItems().size() : 0)")
    WishlistResponse toResponse(WishlistEntity entity);

    @Mapping(target = "productVariantId", source = "productVariant.id")
    @Mapping(target = "productName", source = "productVariant.product.name")
    @Mapping(target = "skuCode", source = "productVariant.product.skuCode")
    @Mapping(target = "price", source = "productVariant.price")
    WishlistItemResponse toItemResponse(WishlistItemEntity item);

    List<WishlistItemResponse> toItemResponseList(List<WishlistItemEntity> items);
}
