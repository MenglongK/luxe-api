package co.istad.corefeature.inventory.facility_inventory.service;

import co.istad.corefeature.facility.entity.FacilityEntity;
import co.istad.corefeature.facility.repository.FacilityRepository;
import co.istad.corefeature.inventory.facility_inventory.dto.FacilityInventoryRequest;
import co.istad.corefeature.inventory.facility_inventory.dto.FacilityInventoryResponse;
import co.istad.corefeature.inventory.facility_inventory.dto.StockRequest;
import co.istad.corefeature.inventory.facility_inventory.entity.FacilityInventoryEntity;
import co.istad.corefeature.inventory.facility_inventory.mapper.FacilityInventoryMapper;
import co.istad.corefeature.inventory.facility_inventory.repository.FacilityInventoryRepository;
import co.istad.corefeature.product.product_variant.entity.ProductVariantEntity;
import co.istad.corefeature.product.product_variant.repository.ProductVariantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FacilityInventoryServiceImpl implements FacilityInventoryService {

    private final FacilityInventoryRepository facilityInventoryRepository;
    private final FacilityRepository facilityRepository;
    private final ProductVariantRepository productVariantRepository;
    private final FacilityInventoryMapper facilityInventoryMapper;

    @Override
    @Transactional
    public FacilityInventoryResponse create(FacilityInventoryRequest request) {
        FacilityEntity facility = facilityRepository.findById(request.facilityId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Facility not found: " + request.facilityId()));

        ProductVariantEntity productVariant = productVariantRepository.findById(request.productVariantId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product variant not found: " + request.productVariantId()));

        if (facilityInventoryRepository.existsByFacilityIdAndProductVariantId(request.facilityId(), request.productVariantId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Inventory already exists for facility and product variant");
        }

        FacilityInventoryEntity entity = facilityInventoryMapper.toEntity(request, facility, productVariant);
        if (entity.getQuantityOnHand() == null) {
            entity.setQuantityOnHand(0);
        }
        if (entity.getReservedQuantity() == null) {
            entity.setReservedQuantity(0);
        }

        entity = facilityInventoryRepository.save(entity);
        return facilityInventoryMapper.toResponse(entity);
    }

    @Override
    public Page<FacilityInventoryResponse> findAll(Pageable pageable) {
        Page<FacilityInventoryEntity> inventoryPage = facilityInventoryRepository.findByIsActiveTrue(pageable);
        return inventoryPage.map(facilityInventoryMapper::toResponse);
    }

    @Override
    public FacilityInventoryResponse findById(Long id) {
        return facilityInventoryRepository.findById(id)
                .map(facilityInventoryMapper::toResponse)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Inventory not found: " + id));
    }

    @Override
    public List<FacilityInventoryResponse> findByFacilityId(Long facilityId) {
        return facilityInventoryRepository.findByFacilityId(facilityId).stream()
                .map(facilityInventoryMapper::toResponse)
                .toList();
    }

    @Override
    public List<FacilityInventoryResponse> findByProductVariantId(Long productVariantId) {
        return facilityInventoryRepository.findByProductVariantId(productVariantId).stream()
                .map(facilityInventoryMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public FacilityInventoryResponse update(Long id, FacilityInventoryRequest request) {
        FacilityInventoryEntity entity = facilityInventoryRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Inventory not found: " + id));

        FacilityEntity facility = facilityRepository.findById(request.facilityId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Facility not found: " + request.facilityId()));

        ProductVariantEntity productVariant = productVariantRepository.findById(request.productVariantId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product variant not found: " + request.productVariantId()));

        Optional<FacilityInventoryEntity> existing = facilityInventoryRepository
                .findByFacilityIdAndProductVariantId(request.facilityId(), request.productVariantId());
        if (existing.isPresent() && !existing.get().getId().equals(id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Inventory already exists for facility and product variant");
        }

        entity.setFacility(facility);
        entity.setProductVariant(productVariant);
        if (request.quantityOnHand() != null) {
            entity.setQuantityOnHand(request.quantityOnHand());
        }
        if (request.reservedQuantity() != null) {
            entity.setReservedQuantity(request.reservedQuantity());
        }

        entity = facilityInventoryRepository.save(entity);
        return facilityInventoryMapper.toResponse(entity);
    }

    @Override
    @Transactional
    public FacilityInventoryResponse reserve(Long id, StockRequest request) {
        FacilityInventoryEntity entity = facilityInventoryRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Inventory not found: " + id));

        int reserved = entity.getReservedQuantity() != null ? entity.getReservedQuantity() : 0;
        int onHand = entity.getQuantityOnHand() != null ? entity.getQuantityOnHand() : 0;

        if (reserved + request.quantity() > onHand) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Insufficient available stock to reserve");
        }

        entity.setReservedQuantity(reserved + request.quantity());
        entity = facilityInventoryRepository.save(entity);
        return facilityInventoryMapper.toResponse(entity);
    }

    @Override
    @Transactional
    public FacilityInventoryResponse release(Long id, StockRequest request) {
        FacilityInventoryEntity entity = facilityInventoryRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Inventory not found: " + id));

        int reserved = entity.getReservedQuantity() != null ? entity.getReservedQuantity() : 0;

        if (reserved < request.quantity()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot release more than the reserved quantity");
        }

        entity.setReservedQuantity(reserved - request.quantity());
        entity = facilityInventoryRepository.save(entity);
        return facilityInventoryMapper.toResponse(entity);
    }

    @Override
    @Transactional
    public FacilityInventoryResponse softDeleteById(Long id) {
        FacilityInventoryEntity entity = facilityInventoryRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Inventory not found: " + id));

        entity.setIsActive(false);
        return facilityInventoryMapper.toResponse(facilityInventoryRepository.save(entity));
    }

    @Override
    @Transactional
    public void permanentDeleteById(Long id) {
        FacilityInventoryEntity entity = facilityInventoryRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Inventory not found: " + id));

        facilityInventoryRepository.delete(entity);
    }
}
