package co.istad.corefeature.order.order_item.repository;

import co.istad.corefeature.order.order_item.entity.OrderItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface OrderItemRepository extends JpaRepository<OrderItemEntity, Long> {

    List<OrderItemEntity> findByOrderId(UUID orderId);
}
