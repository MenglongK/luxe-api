package co.istad.corefeature.user.customer.repository;

import co.istad.corefeature.common.enums.CustomerTier;
import co.istad.corefeature.user.customer.entity.CustomerEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface CustomerRepository extends JpaRepository<CustomerEntity, UUID> {

    Optional<CustomerEntity> findByIdAndIsActiveTrue(UUID id);

    Optional<CustomerEntity> findByCustomerCodeAndIsActiveTrue(String customerCode);

    Optional<CustomerEntity> findByEmailAndIsActiveTrue(String email);

    boolean existsByEmail(String email);

    boolean existsByCustomerCode(String customerCode);

    boolean existsByEmailAndIdNot(String email, UUID id);

    Page<CustomerEntity> findByIsActiveTrue(Pageable pageable);

    @Query("""
        SELECT c FROM CustomerEntity c
        WHERE (:tier IS NULL OR c.tier = :tier)
          AND (
            :search IS NULL OR :search = ''
            OR LOWER(c.name) LIKE LOWER(CONCAT('%', :search, '%'))
            OR LOWER(c.email) LIKE LOWER(CONCAT('%', :search, '%'))
            OR LOWER(c.phone) LIKE LOWER(CONCAT('%', :search, '%'))
            OR LOWER(c.customerCode) LIKE LOWER(CONCAT('%', :search, '%'))
          )
          AND c.isActive = true
    """)
    Page<CustomerEntity> findWithFilters(
            @Param("tier") CustomerTier tier,
            @Param("search") String search,
            Pageable pageable
    );
}
