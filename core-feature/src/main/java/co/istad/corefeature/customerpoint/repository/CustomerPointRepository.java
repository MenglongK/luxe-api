package co.istad.corefeature.customerpoint.repository;

import co.istad.corefeature.common.enums.CustomerTier;
import co.istad.corefeature.customerpoint.entity.CustomerPointEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CustomerPointRepository extends JpaRepository<CustomerPointEntity, Long> {

    boolean existsByCustomerId(String customerId);

    Optional<CustomerPointEntity> findByIdAndIsActiveTrue(Long id);

    Optional<CustomerPointEntity> findByCustomerId(String customerId);

    Optional<CustomerPointEntity> findByCustomerIdAndIsActiveTrue(String customerId);

    Optional<CustomerPointEntity> findByMembershipNumberAndIsActiveTrue(String membershipNumber);

    Page<CustomerPointEntity> findByIsActiveTrue(Pageable pageable);

    @Query("""
        SELECT cp FROM CustomerPointEntity cp
        WHERE cp.isActive = true
          AND (:tier IS NULL OR cp.tier = :tier)
          AND (
            :search IS NULL OR :search = ''
            OR LOWER(cp.customerId) LIKE LOWER(CONCAT('%', :search, '%'))
            OR LOWER(cp.membershipNumber) LIKE LOWER(CONCAT('%', :search, '%'))
          )
    """)
    Page<CustomerPointEntity> findAllWithFilter(
            @Param("tier") CustomerTier tier,
            @Param("search") String search,
            Pageable pageable
    );
}
