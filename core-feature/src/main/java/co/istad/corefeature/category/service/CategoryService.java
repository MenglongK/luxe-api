package co.istad.corefeature.category.service;

import co.istad.corefeature.category.dto.CategoryRequest;
import co.istad.corefeature.category.dto.CategoryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CategoryService {
    CategoryResponse createCategory(CategoryRequest request);

    CategoryResponse updateCategory(CategoryRequest request, Long id);

    Page<CategoryResponse> findAll(Pageable pageable);

    CategoryResponse findById(Long id);

    CategoryResponse softDeleteById(Long id);

    void permanentDeleteById(Long id);
}
