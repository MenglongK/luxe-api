package co.istad.corefeature.facility.service;

import co.istad.corefeature.facility.dto.FacilityRequest;
import co.istad.corefeature.facility.dto.FacilityResponse;
import co.istad.corefeature.facility.entity.FacilityEntity;
import co.istad.corefeature.facility.mapper.FacilityMapper;
import co.istad.corefeature.facility.repository.FacilityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FacilityServiceImpl implements FacilityService {

    private final FacilityRepository facilityRepository;
    private final FacilityMapper facilityMapper;

    @Override
    @Transactional
    public FacilityResponse create(FacilityRequest request) {
        if (facilityRepository.existsByCode(request.code())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Code already exists: " + request.code());
        }

        FacilityEntity entity = facilityMapper.toEntity(request);
        if (entity.getIsActive() == null) {
            entity.setIsActive(true);
        }

        entity = facilityRepository.save(entity);
        return facilityMapper.toResponse(entity);
    }

    @Override
    public Page<FacilityResponse> findAll(Pageable pageable) {
        Page<FacilityEntity> facilityPage = facilityRepository.findByIsActiveTrue(pageable);
        return facilityPage.map(facilityMapper::toResponse);
    }

    @Override
    public FacilityResponse findById(Long id) {
        FacilityEntity entity = facilityRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Facility not found: " + id));
        return facilityMapper.toResponse(entity);
    }

    @Override
    @Transactional
    public FacilityResponse update(Long id, FacilityRequest request) {
        FacilityEntity entity = facilityRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Facility not found: " + id));

        facilityMapper.updateEntityFromRequest(request, entity);

        entity = facilityRepository.save(entity);
        return facilityMapper.toResponse(entity);
    }

    @Override
    @Transactional
    public FacilityResponse softDeleteById(Long id) {
        FacilityEntity entity = facilityRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Facility not found: " + id));

        entity.setIsActive(false);
        return facilityMapper.toResponse(facilityRepository.save(entity));
    }

    @Override
    @Transactional
    public void permanentDeleteById(Long id) {
        FacilityEntity entity = facilityRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Facility not found: " + id));

        facilityRepository.delete(entity);
    }
}
