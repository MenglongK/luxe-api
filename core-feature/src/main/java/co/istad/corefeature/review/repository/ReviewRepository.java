package co.istad.corefeature.review.repository;

import co.istad.corefeature.review.entity.ReviewEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<ReviewEntity, Long> {

    Optional<ReviewEntity> findByIdAndIsActiveTrue(Long id);

    Page<ReviewEntity> findByIsActiveTrue(Pageable pageable);

    Page<ReviewEntity> findByProductIdAndIsActiveTrue(Long productId, Pageable pageable);

    List<ReviewEntity> findByProductIdAndIsActiveTrue(Long productId);

    Page<ReviewEntity> findByCustomerIdAndIsActiveTrue(String customerId, Pageable pageable);

    boolean existsByCustomerIdAndProductIdAndIsActiveTrue(String customerId, Long productId);

    long countByProductIdAndIsActiveTrue(Long productId);
}
