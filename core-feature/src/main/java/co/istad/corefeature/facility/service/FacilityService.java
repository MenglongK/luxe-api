package co.istad.corefeature.facility.service;

import co.istad.corefeature.facility.dto.FacilityRequest;
import co.istad.corefeature.facility.dto.FacilityResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface FacilityService {

    FacilityResponse create(FacilityRequest request);

    Page<FacilityResponse> findAll(Pageable pageable);

    FacilityResponse findById(Long id);

    FacilityResponse update(Long id, FacilityRequest request);

    FacilityResponse softDeleteById(Long id);

    void permanentDeleteById(Long id);
}
