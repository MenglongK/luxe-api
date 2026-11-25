package co.istad.corefeature.category.controller;

import co.istad.corefeature.category.dto.CategoryRequest;
import co.istad.corefeature.category.dto.CategoryResponse;
import co.istad.corefeature.category.service.CategoryService;
import co.istad.corefeature.common.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping({"/api/v1/category", "/api/v1/categories"})
@RequiredArgsConstructor
public class CategoryController {
    private final CategoryService categoryService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<CategoryResponse>>> findAllCategories(
            @ParameterObject @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(ApiResponse.ok("Categories retrieved successfully", categoryService.findAll(pageable)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CategoryResponse>> findCategoryById(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(ApiResponse.ok("Category retrieved successfully", categoryService.findById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CategoryResponse>> createCategory(
            @RequestBody @Valid CategoryRequest categoryRequest
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created("Category created successfully", categoryService.createCategory(categoryRequest)));
    }

    @RequestMapping(value = "/{id}", method = {RequestMethod.PATCH, RequestMethod.PUT})
    public ResponseEntity<ApiResponse<CategoryResponse>> updateCategory(
            @PathVariable Long id,
            @RequestBody @Valid CategoryRequest categoryRequest
    ) {
        return ResponseEntity.ok(ApiResponse.ok("Category updated successfully", categoryService.updateCategory(categoryRequest, id)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<CategoryResponse>> softDeleteCategory(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(ApiResponse.ok("Category soft deleted successfully", categoryService.softDeleteById(id)));
    }

    @DeleteMapping("/{id}/permanent")
    public ResponseEntity<ApiResponse<Void>> permanentDeleteCategory(
            @PathVariable Long id
    ) {
        categoryService.permanentDeleteById(id);
        return ResponseEntity.ok(ApiResponse.ok("Category permanent deleted successfully"));
    }
}
