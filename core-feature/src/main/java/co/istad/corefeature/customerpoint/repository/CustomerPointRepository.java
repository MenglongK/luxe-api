package co.istad.corefeature.customerpoint.repository;

import co.istad.corefeature.customerpoint.entity.CustomerPointEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CustomerPointRepository extends JpaRepository<CustomerPointEntity, Long> {

    boolean existsByCustomerId(String customerId);

    Optional<CustomerPointEntity> findByCustomerId(String customerId);

    Page<CustomerPointEntity> findByIsActiveTrue(Pageable pageable);
}
