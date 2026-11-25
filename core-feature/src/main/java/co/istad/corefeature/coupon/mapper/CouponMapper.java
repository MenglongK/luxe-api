package co.istad.corefeature.coupon.mapper;

import co.istad.corefeature.coupon.dto.CouponRequest;
import co.istad.corefeature.coupon.dto.CouponResponse;
import co.istad.corefeature.coupon.dto.CouponUpdateRequest;
import co.istad.corefeature.coupon.entity.CouponEntity;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface CouponMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "usageCount", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    CouponEntity toEntity(CouponRequest request);

    @Mapping(target = "isCurrentlyUsable", expression = "java(entity.isCurrentlyUsable())")
    CouponResponse toResponse(CouponEntity entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "code", ignore = true)
    @Mapping(target = "usageCount", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromDto(CouponUpdateRequest request, @MappingTarget CouponEntity entity);
}
