package co.istad.corefeature.wallet.repository;

import co.istad.corefeature.common.enums.WalletStatus;
import co.istad.corefeature.wallet.entity.WalletEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface WalletRepository extends JpaRepository<WalletEntity, UUID> {

    boolean existsByCustomerId(String customerId);

    Optional<WalletEntity> findByCustomerIdAndIsActiveTrue(String customerId);

    Optional<WalletEntity> findByCustomerId(String customerId);

    Optional<WalletEntity> findByWalletNumber(String walletNumber);

    Optional<WalletEntity> findByIdAndIsActiveTrue(UUID id);

    @Query("""
            SELECT w FROM WalletEntity w
            WHERE w.isActive = true
              AND (:status IS NULL OR w.status = :status)
              AND (:search IS NULL OR LOWER(w.customerId) LIKE LOWER(CONCAT('%', :search, '%'))
                                 OR LOWER(w.walletNumber) LIKE LOWER(CONCAT('%', :search, '%')))
            """)
    Page<WalletEntity> findAllWithFilter(
            @Param("status") WalletStatus status,
            @Param("search") String search,
            Pageable pageable
    );
}
