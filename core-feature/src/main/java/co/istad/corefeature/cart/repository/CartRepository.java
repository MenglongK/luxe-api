package co.istad.corefeature.cart.repository;

import co.istad.corefeature.cart.entity.CartEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CartRepository extends JpaRepository<CartEntity, UUID> {

    boolean existsByCustomerId(String customerId);

    Optional<CartEntity> findByCustomerId(String customerId);

    Page<CartEntity> findByIsActiveTrue(Pageable pageable);
}
