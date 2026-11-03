package co.istad.corefeature.brand.repository;

import co.istad.corefeature.brand.entity.BrandEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BrandRepository extends JpaRepository<BrandEntity, Long> {
    boolean existsByName(String name);

    Page<BrandEntity> findByIsActiveTrue(Pageable pageable);
}
