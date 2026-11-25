package co.istad.corefeature.supplier.service;

import co.istad.corefeature.common.enums.SupplierCategory;
import co.istad.corefeature.common.enums.SupplierStatus;
import co.istad.corefeature.supplier.dto.SupplierRequest;
import co.istad.corefeature.supplier.dto.SupplierResponse;
import co.istad.corefeature.supplier.dto.SupplierUpdateRequest;
import co.istad.corefeature.supplier.entity.SupplierEntity;
import co.istad.corefeature.supplier.mapper.SupplierMapper;
import co.istad.corefeature.supplier.repository.SupplierRepository;
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
public class SupplierServiceImpl implements SupplierService {

    private final SupplierRepository supplierRepository;
    private final SupplierMapper supplierMapper;

    @Override
    @Transactional
    public SupplierResponse create(SupplierRequest request) {
        String normalizedCode = request.code().trim().toUpperCase();
        String normalizedEmail = request.email().trim().toLowerCase();

        if (supplierRepository.existsByCodeIgnoreCase(normalizedCode)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Supplier with code " + normalizedCode + " already exists");
        }

        if (supplierRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Supplier with email " + normalizedEmail + " already exists");
        }

        SupplierEntity supplier = supplierMapper.toEntity(request);
        supplier.setCode(normalizedCode);
        supplier.setEmail(normalizedEmail);
        supplier.setIsActive(true);

        if (supplier.getStatus() == null) {
            supplier.setStatus(SupplierStatus.ACTIVE);
        }

        SupplierEntity saved = supplierRepository.save(supplier);
        return supplierMapper.toResponse(saved);
    }

    @Override
    public Page<SupplierResponse> findAll(SupplierCategory category, SupplierStatus status, String search, Pageable pageable) {
        String query = (search != null && !search.isBlank()) ? search.trim() : null;
        Page<SupplierEntity> page;
        if (category == null && status == null && query == null) {
            page = supplierRepository.findByIsActiveTrue(pageable);
        } else {
            page = supplierRepository.findWithFilters(category, status, query, pageable);
        }
        return page.map(supplierMapper::toResponse);
    }

    @Override
    public SupplierResponse findById(Long id) {
        SupplierEntity supplier = supplierRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Supplier not found with ID: " + id));
        return supplierMapper.toResponse(supplier);
    }

    @Override
    public SupplierResponse findByCode(String code) {
        SupplierEntity supplier = supplierRepository.findByCodeIgnoreCaseAndIsActiveTrue(code.trim())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Supplier not found with code: " + code));
        return supplierMapper.toResponse(supplier);
    }

    @Override
    @Transactional
    public SupplierResponse update(Long id, SupplierUpdateRequest request) {
        SupplierEntity supplier = supplierRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Supplier not found with ID: " + id));

        if (request.email() != null && !request.email().isBlank()) {
            String normalizedEmail = request.email().trim().toLowerCase();
            if (!normalizedEmail.equalsIgnoreCase(supplier.getEmail()) && supplierRepository.existsByEmailIgnoreCaseAndIdNot(normalizedEmail, id)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Email " + request.email() + " is already in use by another supplier");
            }
            supplier.setEmail(normalizedEmail);
        }

        supplierMapper.updateEntityFromDto(request, supplier);
        SupplierEntity updated = supplierRepository.save(supplier);
        return supplierMapper.toResponse(updated);
    }

    @Override
    @Transactional
    public SupplierResponse softDeleteById(Long id) {
        SupplierEntity supplier = supplierRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Supplier not found with ID: " + id));

        supplier.setIsActive(false);
        SupplierEntity saved = supplierRepository.save(supplier);
        return supplierMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public void permanentDeleteById(Long id) {
        SupplierEntity supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Supplier not found with ID: " + id));
        supplierRepository.delete(supplier);
    }
}
