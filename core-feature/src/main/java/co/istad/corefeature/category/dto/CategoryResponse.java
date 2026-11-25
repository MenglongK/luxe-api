package co.istad.corefeature.category.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record CategoryResponse(
        Long id,
        String name,
        Long parentId,
        List<CategoryResponse> subCategories,
        Boolean isActive
) {
}
