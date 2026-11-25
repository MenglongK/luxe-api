package co.istad.corefeature.wallet.repository;

import co.istad.corefeature.wallet.entity.WalletTransactionEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface WalletTransactionRepository extends JpaRepository<WalletTransactionEntity, UUID> {

    Page<WalletTransactionEntity> findByWalletIdOrderByCreatedAtDesc(UUID walletId, Pageable pageable);

    Page<WalletTransactionEntity> findByCustomerIdOrderByCreatedAtDesc(String customerId, Pageable pageable);
}
