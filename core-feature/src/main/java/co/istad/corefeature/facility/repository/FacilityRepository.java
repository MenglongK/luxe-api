package co.istad.corefeature.facility.repository;

import co.istad.corefeature.facility.entity.FacilityEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FacilityRepository extends JpaRepository<FacilityEntity, Long> {

    boolean existsByCode(String code);

    Page<FacilityEntity> findByIsActiveTrue(Pageable pageable);
}
