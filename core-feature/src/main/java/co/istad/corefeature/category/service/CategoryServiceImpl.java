package co.istad.corefeature.category.service;

import co.istad.corefeature.category.dto.CategoryRequest;
import co.istad.corefeature.category.dto.CategoryResponse;
import co.istad.corefeature.category.entity.CategoryEntity;
import co.istad.corefeature.category.mapper.CategoryMapper;
import co.istad.corefeature.category.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    @Override
    @Transactional
    public CategoryResponse createCategory(CategoryRequest request) {
        boolean isSubCategory = request.parentId() != null && request.parentId() > 0;

        CategoryEntity categoryEntity = categoryMapper.toEntity(request);
        if (categoryEntity.getIsActive() == null) {
            categoryEntity.setIsActive(true);
        }

        if (isSubCategory) {
            CategoryEntity parentEntity = categoryRepository.findById(request.parentId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Parent category not found"));

            if (categoryRepository.existsByNameAndParentCategoryId(request.name(), request.parentId())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Category already exists under this parent");
            }

            categoryEntity.setParentCategory(parentEntity);
        } else {
            if (categoryRepository.existsByNameAndParentCategoryIsNull(request.name())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Main category already exists");
            }
            categoryEntity.setParentCategory(null);
        }

        return categoryMapper.toResponse(categoryRepository.save(categoryEntity));
    }

    @Override
    @Transactional
    public CategoryResponse updateCategory(CategoryRequest request, Long id) {
        CategoryEntity categoryEntity = categoryRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found"));

        Long targetParentId = request.parentId() != null
                ? (request.parentId() > 0 ? request.parentId() : null)
                : (categoryEntity.getParentCategory() != null ? categoryEntity.getParentCategory().getId() : null);

        if (targetParentId != null && targetParentId.equals(id)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Category cannot be its own parent");
        }

        boolean nameChanged = !categoryEntity.getName().equals(request.name());
        boolean parentChanged = (categoryEntity.getParentCategory() == null && targetParentId != null)
                || (categoryEntity.getParentCategory() != null && !categoryEntity.getParentCategory().getId().equals(targetParentId));

        if (nameChanged || parentChanged) {
            if (targetParentId != null) {
                if (categoryRepository.existsByNameAndParentCategoryId(request.name(), targetParentId)) {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Category already exists under the target parent");
                }
            } else {
                if (categoryRepository.existsByNameAndParentCategoryIsNull(request.name())) {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Main category already exists");
                }
            }
        }

        categoryMapper.updateEntityFromDto(request, categoryEntity);

        if (request.parentId() != null) {
            if (request.parentId() > 0) {
                CategoryEntity parentEntity = categoryRepository.findById(request.parentId())
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Parent category not found"));
                categoryEntity.setParentCategory(parentEntity);
            } else {
                categoryEntity.setParentCategory(null);
            }
        }

        return categoryMapper.toResponse(categoryRepository.save(categoryEntity));
    }

    @Override
    public Page<CategoryResponse> findAll(Pageable pageable) {
        Page<CategoryEntity> categoryPage = categoryRepository.findByParentCategoryIsNullAndIsActiveTrue(pageable);
        return categoryPage.map(categoryMapper::toResponse);
    }

    @Override
    public CategoryResponse findById(Long id) {
        return categoryRepository.findById(id).map(categoryMapper::toResponse).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found"));
    }

    @Override
    @Transactional
    public CategoryResponse softDeleteById(Long id) {
        CategoryEntity categoryEntity = categoryRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found"));
        categoryEntity.setIsActive(false);
        if (categoryEntity.getSubCategories() != null) {
            categoryEntity.getSubCategories().forEach(sub -> sub.setIsActive(false));
        }
        return categoryMapper.toResponse(categoryRepository.save(categoryEntity));
    }

    @Override
    @Transactional
    public void permanentDeleteById(Long id) {
        CategoryEntity categoryEntity = categoryRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found"));
        categoryRepository.delete(categoryEntity);
    }
}
