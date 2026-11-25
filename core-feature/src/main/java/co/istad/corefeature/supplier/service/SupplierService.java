package co.istad.corefeature.supplier.service;

import co.istad.corefeature.common.enums.SupplierCategory;
import co.istad.corefeature.common.enums.SupplierStatus;
import co.istad.corefeature.supplier.dto.SupplierRequest;
import co.istad.corefeature.supplier.dto.SupplierResponse;
import co.istad.corefeature.supplier.dto.SupplierUpdateRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface SupplierService {

    SupplierResponse create(SupplierRequest request);

    Page<SupplierResponse> findAll(SupplierCategory category, SupplierStatus status, String search, Pageable pageable);

    SupplierResponse findById(Long id);

    SupplierResponse findByCode(String code);

    SupplierResponse update(Long id, SupplierUpdateRequest request);

    SupplierResponse softDeleteById(Long id);

    void permanentDeleteById(Long id);
}
