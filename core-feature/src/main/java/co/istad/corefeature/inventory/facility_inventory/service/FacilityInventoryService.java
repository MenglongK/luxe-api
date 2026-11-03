package co.istad.corefeature.inventory.facility_inventory.service;

import co.istad.corefeature.inventory.facility_inventory.dto.FacilityInventoryRequest;
import co.istad.corefeature.inventory.facility_inventory.dto.FacilityInventoryResponse;
import co.istad.corefeature.inventory.facility_inventory.dto.StockRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface FacilityInventoryService {

    FacilityInventoryResponse create(FacilityInventoryRequest request);

    Page<FacilityInventoryResponse> findAll(Pageable pageable);

    FacilityInventoryResponse findById(Long id);

    List<FacilityInventoryResponse> findByFacilityId(Long facilityId);

    List<FacilityInventoryResponse> findByProductVariantId(Long productVariantId);

    FacilityInventoryResponse update(Long id, FacilityInventoryRequest request);

    FacilityInventoryResponse reserve(Long id, StockRequest request);

    FacilityInventoryResponse release(Long id, StockRequest request);

    FacilityInventoryResponse softDeleteById(Long id);

    void permanentDeleteById(Long id);
}
