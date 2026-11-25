package co.istad.corefeature.customerpoint.repository;

import co.istad.corefeature.customerpoint.entity.CustomerPointTransactionEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerPointTransactionRepository extends JpaRepository<CustomerPointTransactionEntity, Long> {

    Page<CustomerPointTransactionEntity> findByCustomerPointIdOrderByCreatedAtDesc(Long customerPointId, Pageable pageable);

    Page<CustomerPointTransactionEntity> findByCustomerIdOrderByCreatedAtDesc(String customerId, Pageable pageable);
}
