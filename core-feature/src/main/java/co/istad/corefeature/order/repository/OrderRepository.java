package co.istad.corefeature.order.repository;

import co.istad.corefeature.order.entity.OrderEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<OrderEntity, UUID> {

    Optional<OrderEntity> findByOrderNumber(String orderNumber);

    Optional<OrderEntity> findByIdAndIsActiveTrue(UUID id);

    Page<OrderEntity> findByIsActiveTrue(Pageable pageable);

    Page<OrderEntity> findByCustomerIdAndIsActiveTrue(String customerId, Pageable pageable);

    List<OrderEntity> findByCustomerIdAndIsActiveTrue(String customerId);

    long countByCustomerIdAndIsActiveTrue(String customerId);

    boolean existsByOrderNumber(String orderNumber);
}
