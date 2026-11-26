package co.istad.corefeature.customerpoint.mapper;

import co.istad.corefeature.common.enums.CustomerTier;
import co.istad.corefeature.customerpoint.dto.CustomerPointRequest;
import co.istad.corefeature.customerpoint.dto.CustomerPointResponse;
import co.istad.corefeature.customerpoint.entity.CustomerPointEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CustomerPointMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "membershipNumber", ignore = true)
    @Mapping(target = "lifetimePoints", ignore = true)
    @Mapping(target = "tier", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    CustomerPointEntity toEntity(CustomerPointRequest request);

    @Mapping(target = "nextTier", expression = "java(resolveNextTier(entity))")
    @Mapping(target = "pointsToNextTier", expression = "java(resolvePointsToNextTier(entity))")
    CustomerPointResponse toResponse(CustomerPointEntity entity);

    default CustomerTier resolveNextTier(CustomerPointEntity entity) {
        CustomerTier currentTier = entity.getTier() != null ? entity.getTier() : CustomerTier.STANDARD;
        return currentTier.nextTier();
    }

    default Integer resolvePointsToNextTier(CustomerPointEntity entity) {
        CustomerTier currentTier = entity.getTier() != null ? entity.getTier() : CustomerTier.STANDARD;
        int lifetime = entity.getLifetimePoints() != null ? entity.getLifetimePoints() : 0;
        return currentTier.pointsToNextTier(lifetime);
    }
}
