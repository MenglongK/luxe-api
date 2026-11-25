package co.istad.corefeature.category.service;

import co.istad.corefeature.category.dto.CategoryRequest;
import co.istad.corefeature.category.dto.CategoryResponse;
import co.istad.corefeature.category.entity.CategoryEntity;
import co.istad.corefeature.category.mapper.CategoryMapper;
import co.istad.corefeature.category.mapper.CategoryMapperImpl;
import co.istad.corefeature.category.repository.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceImplTest {

    @Mock
    private CategoryRepository categoryRepository;

    private CategoryMapper categoryMapper;
    private CategoryService categoryService;

    @BeforeEach
    void setUp() {
        categoryMapper = new CategoryMapperImpl();
        categoryService = new CategoryServiceImpl(categoryRepository, categoryMapper);
    }

    @Test
    @DisplayName("Should successfully create a main category when parentId is null")
    void createCategory_mainCategory_success() {
        CategoryRequest request = new CategoryRequest("Men", null, true);

        when(categoryRepository.existsByNameAndParentCategoryIsNull("Men")).thenReturn(false);
        when(categoryRepository.save(any(CategoryEntity.class))).thenAnswer(invocation -> {
            CategoryEntity entity = invocation.getArgument(0);
            entity.setId(1L);
            return entity;
        });

        CategoryResponse response = categoryService.createCategory(request);

        assertNotNull(response);
        assertEquals(1L, response.id());
        assertEquals("Men", response.name());
        assertNull(response.parentId());
        assertTrue(response.isActive());
        verify(categoryRepository).save(any(CategoryEntity.class));
    }

    @Test
    @DisplayName("Should treat parentId of 0 as a main category")
    void createCategory_mainCategory_withZeroParentId_success() {
        CategoryRequest request = new CategoryRequest("Women", 0L, null);

        when(categoryRepository.existsByNameAndParentCategoryIsNull("Women")).thenReturn(false);
        when(categoryRepository.save(any(CategoryEntity.class))).thenAnswer(invocation -> {
            CategoryEntity entity = invocation.getArgument(0);
            entity.setId(2L);
            return entity;
        });

        CategoryResponse response = categoryService.createCategory(request);

        assertNotNull(response);
        assertEquals(2L, response.id());
        assertEquals("Women", response.name());
        assertNull(response.parentId());
        assertTrue(response.isActive(), "Should default isActive to true when not provided");
    }

    @Test
    @DisplayName("Should throw 409 CONFLICT if main category already exists")
    void createCategory_mainCategory_conflict() {
        CategoryRequest request = new CategoryRequest("Men", null, true);

        when(categoryRepository.existsByNameAndParentCategoryIsNull("Men")).thenReturn(true);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
                categoryService.createCategory(request)
        );

        assertEquals(409, exception.getStatusCode().value());
        assertTrue(exception.getReason().contains("Main category already exists"));
        verify(categoryRepository, never()).save(any(CategoryEntity.class));
    }

    @Test
    @DisplayName("Should successfully create a subcategory with a valid parent category")
    void createCategory_subCategory_success() {
        CategoryEntity parentEntity = CategoryEntity.builder()
                .id(1L)
                .name("Men")
                .isActive(true)
                .build();

        CategoryRequest request = new CategoryRequest("Shirts", 1L, true);

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(parentEntity));
        when(categoryRepository.existsByNameAndParentCategoryId("Shirts", 1L)).thenReturn(false);
        when(categoryRepository.save(any(CategoryEntity.class))).thenAnswer(invocation -> {
            CategoryEntity entity = invocation.getArgument(0);
            entity.setId(10L);
            return entity;
        });

        CategoryResponse response = categoryService.createCategory(request);

        assertNotNull(response);
        assertEquals(10L, response.id());
        assertEquals("Shirts", response.name());
        assertEquals(1L, response.parentId());
        assertTrue(response.isActive());
    }

    @Test
    @DisplayName("Should throw 404 NOT_FOUND if parent category does not exist")
    void createCategory_subCategory_parentNotFound() {
        CategoryRequest request = new CategoryRequest("Shirts", 999L, true);

        when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
                categoryService.createCategory(request)
        );

        assertEquals(404, exception.getStatusCode().value());
        assertTrue(exception.getReason().contains("Parent category not found"));
    }

    @Test
    @DisplayName("Should throw 409 CONFLICT if subcategory already exists under parent")
    void createCategory_subCategory_conflict() {
        CategoryEntity parentEntity = CategoryEntity.builder()
                .id(1L)
                .name("Men")
                .build();

        CategoryRequest request = new CategoryRequest("Shirts", 1L, true);

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(parentEntity));
        when(categoryRepository.existsByNameAndParentCategoryId("Shirts", 1L)).thenReturn(true);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
                categoryService.createCategory(request)
        );

        assertEquals(409, exception.getStatusCode().value());
        assertTrue(exception.getReason().contains("Category already exists under this parent"));
    }

    @Test
    @DisplayName("Should find category by id")
    void findById_success() {
        CategoryEntity entity = CategoryEntity.builder()
                .id(1L)
                .name("Men")
                .isActive(true)
                .build();

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(entity));

        CategoryResponse response = categoryService.findById(1L);

        assertNotNull(response);
        assertEquals(1L, response.id());
        assertEquals("Men", response.name());
    }

    @Test
    @DisplayName("Should throw 404 NOT_FOUND if category does not exist by id")
    void findById_notFound() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> categoryService.findById(1L));
    }

    @Test
    @DisplayName("Should find all root categories with pagination")
    void findAll_returnsRootCategories() {
        CategoryEntity entity = CategoryEntity.builder()
                .id(1L)
                .name("Men")
                .isActive(true)
                .build();

        Pageable pageable = PageRequest.of(0, 10);
        when(categoryRepository.findByParentCategoryIsNullAndIsActiveTrue(pageable))
                .thenReturn(new PageImpl<>(List.of(entity), pageable, 1));

        Page<CategoryResponse> responses = categoryService.findAll(pageable);

        assertEquals(1, responses.getTotalElements());
        assertEquals("Men", responses.getContent().get(0).name());
    }

    @Test
    @DisplayName("Should update category successfully")
    void updateCategory_success() {
        CategoryEntity entity = CategoryEntity.builder()
                .id(1L)
                .name("Old Name")
                .isActive(true)
                .build();

        CategoryRequest request = new CategoryRequest("New Name", null, true);

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(categoryRepository.existsByNameAndParentCategoryIsNull("New Name")).thenReturn(false);
        when(categoryRepository.save(any(CategoryEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CategoryResponse response = categoryService.updateCategory(request, 1L);

        assertNotNull(response);
        assertEquals("New Name", response.name());
    }

    @Test
    @DisplayName("Should reject category being its own parent")
    void updateCategory_cannotBeOwnParent() {
        CategoryEntity entity = CategoryEntity.builder()
                .id(1L)
                .name("Men")
                .isActive(true)
                .build();

        CategoryRequest request = new CategoryRequest("Men", 1L, true);

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(entity));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
                categoryService.updateCategory(request, 1L)
        );

        assertEquals(400, exception.getStatusCode().value());
        assertTrue(exception.getReason().contains("Category cannot be its own parent"));
    }

    @Test
    @DisplayName("Should soft delete category and deactivate subcategories")
    void softDeleteById_success() {
        CategoryEntity sub = CategoryEntity.builder()
                .id(2L)
                .name("Shirts")
                .isActive(true)
                .build();

        List<CategoryEntity> subs = new ArrayList<>();
        subs.add(sub);

        CategoryEntity entity = CategoryEntity.builder()
                .id(1L)
                .name("Men")
                .isActive(true)
                .subCategories(subs)
                .build();

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(categoryRepository.save(any(CategoryEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CategoryResponse response = categoryService.softDeleteById(1L);

        assertNotNull(response);
        assertFalse(response.isActive());
        assertFalse(sub.getIsActive(), "Subcategories should also be deactivated");
    }

    @Test
    @DisplayName("Should permanently delete category")
    void permanentDeleteById_success() {
        CategoryEntity entity = CategoryEntity.builder()
                .id(1L)
                .name("Men")
                .build();

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(entity));

        categoryService.permanentDeleteById(1L);

        verify(categoryRepository).delete(entity);
    }
}
