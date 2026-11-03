package co.istad.corefeature.inventory.facility_inventory.repository;

import co.istad.corefeature.inventory.facility_inventory.entity.FacilityInventoryEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FacilityInventoryRepository extends JpaRepository<FacilityInventoryEntity, Long> {

    boolean existsByFacilityIdAndProductVariantId(Long facilityId, Long productVariantId);

    Optional<FacilityInventoryEntity> findByFacilityIdAndProductVariantId(Long facilityId, Long productVariantId);

    List<FacilityInventoryEntity> findByFacilityId(Long facilityId);

    List<FacilityInventoryEntity> findByProductVariantId(Long productVariantId);

    Page<FacilityInventoryEntity> findByIsActiveTrue(Pageable pageable);
}
