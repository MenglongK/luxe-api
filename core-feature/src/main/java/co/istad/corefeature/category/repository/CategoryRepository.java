package co.istad.corefeature.category.repository;

import co.istad.corefeature.category.entity.CategoryEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<CategoryEntity, Long> {
    boolean existsByName(String name);

    boolean existsByNameAndParentCategoryIsNull(String name);

    boolean existsByNameAndParentCategoryId(String name, Long parentId);

    Page<CategoryEntity> findByParentCategoryIsNullAndIsActiveTrue(Pageable pageable);
}
