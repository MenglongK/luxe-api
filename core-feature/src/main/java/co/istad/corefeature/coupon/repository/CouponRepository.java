package co.istad.corefeature.coupon.repository;

import co.istad.corefeature.common.enums.CouponStatus;
import co.istad.corefeature.common.enums.DiscountType;
import co.istad.corefeature.coupon.entity.CouponEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface CouponRepository extends JpaRepository<CouponEntity, UUID> {

    Optional<CouponEntity> findByIdAndIsActiveTrue(UUID id);

    Optional<CouponEntity> findByCodeIgnoreCaseAndIsActiveTrue(String code);

    boolean existsByCodeIgnoreCase(String code);

    boolean existsByCodeIgnoreCaseAndIdNot(String code, UUID id);

    Page<CouponEntity> findByIsActiveTrue(Pageable pageable);

    @Query("""
        SELECT c FROM CouponEntity c
        WHERE (:status IS NULL OR c.status = :status)
          AND (:discountType IS NULL OR c.discountType = :discountType)
          AND (
            :search IS NULL OR :search = ''
            OR LOWER(c.code) LIKE LOWER(CONCAT('%', :search, '%'))
            OR LOWER(c.description) LIKE LOWER(CONCAT('%', :search, '%'))
          )
          AND c.isActive = true
    """)
    Page<CouponEntity> findWithFilters(
            @Param("status") CouponStatus status,
            @Param("discountType") DiscountType discountType,
            @Param("search") String search,
            Pageable pageable
    );
}
