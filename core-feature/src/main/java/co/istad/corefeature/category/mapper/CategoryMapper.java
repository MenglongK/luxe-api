package co.istad.corefeature.category.mapper;

import co.istad.corefeature.category.dto.CategoryRequest;
import co.istad.corefeature.category.dto.CategoryResponse;
import co.istad.corefeature.category.entity.CategoryEntity;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface CategoryMapper {
    @Mapping(target = "parentCategory", ignore = true)
    @Mapping(target = "subCategories", ignore = true)
    CategoryEntity toEntity(CategoryRequest categoryRequest);

    @Mapping(source = "parentCategory.id", target = "parentId")
    CategoryResponse toResponse(CategoryEntity categoryEntity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "parentCategory", ignore = true)
    @Mapping(target = "subCategories", ignore = true)
    void updateEntityFromDto(CategoryRequest request, @MappingTarget CategoryEntity entity);
}
