package co.istad.corefeature.supplier.repository;

import co.istad.corefeature.common.enums.SupplierCategory;
import co.istad.corefeature.common.enums.SupplierStatus;
import co.istad.corefeature.supplier.entity.SupplierEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface SupplierRepository extends JpaRepository<SupplierEntity, Long> {

    Optional<SupplierEntity> findByIdAndIsActiveTrue(Long id);

    Optional<SupplierEntity> findByCodeIgnoreCaseAndIsActiveTrue(String code);

    boolean existsByCodeIgnoreCase(String code);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);

    Page<SupplierEntity> findByIsActiveTrue(Pageable pageable);

    @Query("""
        SELECT s FROM SupplierEntity s
        WHERE (:category IS NULL OR s.category = :category)
          AND (:status IS NULL OR s.status = :status)
          AND (
            :search IS NULL OR :search = ''
            OR LOWER(s.name) LIKE LOWER(CONCAT('%', :search, '%'))
            OR LOWER(s.code) LIKE LOWER(CONCAT('%', :search, '%'))
            OR LOWER(s.contactPerson) LIKE LOWER(CONCAT('%', :search, '%'))
            OR LOWER(s.email) LIKE LOWER(CONCAT('%', :search, '%'))
          )
          AND s.isActive = true
    """)
    Page<SupplierEntity> findWithFilters(
            @Param("category") SupplierCategory category,
            @Param("status") SupplierStatus status,
            @Param("search") String search,
            Pageable pageable
    );
}
