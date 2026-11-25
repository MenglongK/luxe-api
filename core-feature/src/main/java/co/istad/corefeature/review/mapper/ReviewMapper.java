package co.istad.corefeature.review.mapper;

import co.istad.corefeature.review.dto.ReviewRequest;
import co.istad.corefeature.review.dto.ReviewResponse;
import co.istad.corefeature.review.dto.ReviewUpdateRequest;
import co.istad.corefeature.review.entity.ReviewEntity;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface ReviewMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "product", ignore = true)
    @Mapping(target = "isVerifiedBuyer", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    ReviewEntity toEntity(ReviewRequest request);

    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "productName", source = "product.name")
    ReviewResponse toResponse(ReviewEntity entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "product", ignore = true)
    @Mapping(target = "customerId", ignore = true)
    @Mapping(target = "authorName", ignore = true)
    @Mapping(target = "isVerifiedBuyer", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromRequest(ReviewUpdateRequest request, @MappingTarget ReviewEntity entity);
}
