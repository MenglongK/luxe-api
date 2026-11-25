package co.istad.corefeature.supplier.service;

import co.istad.corefeature.common.enums.PaymentTerms;
import co.istad.corefeature.common.enums.SupplierCategory;
import co.istad.corefeature.common.enums.SupplierStatus;
import co.istad.corefeature.supplier.dto.SupplierRequest;
import co.istad.corefeature.supplier.dto.SupplierResponse;
import co.istad.corefeature.supplier.dto.SupplierUpdateRequest;
import co.istad.corefeature.supplier.entity.SupplierEntity;
import co.istad.corefeature.supplier.mapper.SupplierMapper;
import co.istad.corefeature.supplier.repository.SupplierRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SupplierServiceImplTest {

    @Mock
    private SupplierRepository supplierRepository;

    @Mock
    private SupplierMapper supplierMapper;

    private SupplierService supplierService;

    private Long supplierId;
    private SupplierEntity supplierEntity;
    private SupplierRequest supplierRequest;
    private SupplierResponse supplierResponse;

    @BeforeEach
    void setUp() {
        supplierService = new SupplierServiceImpl(supplierRepository, supplierMapper);

        supplierId = 1L;

        supplierEntity = SupplierEntity.builder()
                .id(supplierId)
                .code("SUP-LP-01")
                .name("Lanificio Loro Piana & C.")
                .category(SupplierCategory.ITALIAN_WOOL)
                .contactPerson("Matteo Casagrande")
                .email("matteo.c@loropiana-b2b.it")
                .phone("+39 015 7711")
                .address("Corso Rolandi 10, Quarona, Italy")
                .rating(5)
                .leadTimeDays(21)
                .paymentTerms(PaymentTerms.NET_60)
                .status(SupplierStatus.ACTIVE)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        supplierRequest = SupplierRequest.builder()
                .code("SUP-LP-01")
                .name("Lanificio Loro Piana & C.")
                .category(SupplierCategory.ITALIAN_WOOL)
                .contactPerson("Matteo Casagrande")
                .email("matteo.c@loropiana-b2b.it")
                .phone("+39 015 7711")
                .address("Corso Rolandi 10, Quarona, Italy")
                .rating(5)
                .leadTimeDays(21)
                .paymentTerms(PaymentTerms.NET_60)
                .status(SupplierStatus.ACTIVE)
                .build();

        supplierResponse = SupplierResponse.builder()
                .id(supplierId)
                .code("SUP-LP-01")
                .name("Lanificio Loro Piana & C.")
                .category(SupplierCategory.ITALIAN_WOOL)
                .contactPerson("Matteo Casagrande")
                .email("matteo.c@loropiana-b2b.it")
                .phone("+39 015 7711")
                .address("Corso Rolandi 10, Quarona, Italy")
                .rating(5)
                .leadTimeDays(21)
                .paymentTerms(PaymentTerms.NET_60)
                .status(SupplierStatus.ACTIVE)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("create - successfully creates and returns a supplier")
    void create_success() {
        when(supplierRepository.existsByCodeIgnoreCase("SUP-LP-01")).thenReturn(false);
        when(supplierRepository.existsByEmailIgnoreCase("matteo.c@loropiana-b2b.it")).thenReturn(false);
        when(supplierMapper.toEntity(supplierRequest)).thenReturn(supplierEntity);
        when(supplierRepository.save(any(SupplierEntity.class))).thenReturn(supplierEntity);
        when(supplierMapper.toResponse(supplierEntity)).thenReturn(supplierResponse);

        SupplierResponse result = supplierService.create(supplierRequest);

        assertNotNull(result);
        assertEquals("SUP-LP-01", result.code());
        assertEquals("Lanificio Loro Piana & C.", result.name());
        assertEquals(SupplierCategory.ITALIAN_WOOL, result.category());
        verify(supplierRepository, times(1)).save(any(SupplierEntity.class));
    }

    @Test
    @DisplayName("create - throws 409 conflict when supplier code already exists")
    void create_duplicateCode_throwsConflict() {
        when(supplierRepository.existsByCodeIgnoreCase("SUP-LP-01")).thenReturn(true);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> supplierService.create(supplierRequest));

        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
        assertTrue(ex.getReason().contains("already exists"));
        verify(supplierRepository, never()).save(any());
    }

    @Test
    @DisplayName("create - throws 409 conflict when email already exists")
    void create_duplicateEmail_throwsConflict() {
        when(supplierRepository.existsByCodeIgnoreCase("SUP-LP-01")).thenReturn(false);
        when(supplierRepository.existsByEmailIgnoreCase("matteo.c@loropiana-b2b.it")).thenReturn(true);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> supplierService.create(supplierRequest));

        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
        assertTrue(ex.getReason().contains("already exists"));
        verify(supplierRepository, never()).save(any());
    }

    @Test
    @DisplayName("findAll - unfiltered returns all active suppliers")
    void findAll_unfiltered_success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<SupplierEntity> page = new PageImpl<>(List.of(supplierEntity), pageable, 1);

        when(supplierRepository.findByIsActiveTrue(pageable)).thenReturn(page);
        when(supplierMapper.toResponse(supplierEntity)).thenReturn(supplierResponse);

        Page<SupplierResponse> result = supplierService.findAll(null, null, null, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("SUP-LP-01", result.getContent().get(0).code());
        verify(supplierRepository, times(1)).findByIsActiveTrue(pageable);
        verify(supplierRepository, never()).findWithFilters(any(), any(), any(), any());
    }

    @Test
    @DisplayName("findAll - with filters applies criteria")
    void findAll_withFilters_success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<SupplierEntity> page = new PageImpl<>(List.of(supplierEntity), pageable, 1);

        when(supplierRepository.findWithFilters(SupplierCategory.ITALIAN_WOOL, SupplierStatus.ACTIVE, "Loro", pageable))
                .thenReturn(page);
        when(supplierMapper.toResponse(supplierEntity)).thenReturn(supplierResponse);

        Page<SupplierResponse> result = supplierService.findAll(SupplierCategory.ITALIAN_WOOL, SupplierStatus.ACTIVE, "Loro", pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(supplierRepository, times(1)).findWithFilters(SupplierCategory.ITALIAN_WOOL, SupplierStatus.ACTIVE, "Loro", pageable);
    }

    @Test
    @DisplayName("findById - successfully returns supplier when found and active")
    void findById_success() {
        when(supplierRepository.findByIdAndIsActiveTrue(supplierId)).thenReturn(Optional.of(supplierEntity));
        when(supplierMapper.toResponse(supplierEntity)).thenReturn(supplierResponse);

        SupplierResponse result = supplierService.findById(supplierId);

        assertNotNull(result);
        assertEquals(supplierId, result.id());
        assertEquals("SUP-LP-01", result.code());
    }

    @Test
    @DisplayName("findById - throws 404 when supplier not found")
    void findById_notFound_throwsException() {
        when(supplierRepository.findByIdAndIsActiveTrue(999L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> supplierService.findById(999L));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        assertTrue(ex.getReason().contains("Supplier not found"));
    }

    @Test
    @DisplayName("findByCode - successfully returns supplier by code")
    void findByCode_success() {
        when(supplierRepository.findByCodeIgnoreCaseAndIsActiveTrue("SUP-LP-01")).thenReturn(Optional.of(supplierEntity));
        when(supplierMapper.toResponse(supplierEntity)).thenReturn(supplierResponse);

        SupplierResponse result = supplierService.findByCode("SUP-LP-01");

        assertNotNull(result);
        assertEquals("SUP-LP-01", result.code());
    }

    @Test
    @DisplayName("findByCode - throws 404 when code not found")
    void findByCode_notFound_throwsException() {
        when(supplierRepository.findByCodeIgnoreCaseAndIsActiveTrue("NON_EXISTING")).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> supplierService.findByCode("NON_EXISTING"));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    @DisplayName("update - successfully updates supplier fields")
    void update_success() {
        SupplierUpdateRequest updateRequest = SupplierUpdateRequest.builder()
                .name("Loro Piana Luxury Fabrics")
                .leadTimeDays(18)
                .rating(5)
                .build();

        when(supplierRepository.findByIdAndIsActiveTrue(supplierId)).thenReturn(Optional.of(supplierEntity));
        when(supplierRepository.save(supplierEntity)).thenReturn(supplierEntity);
        when(supplierMapper.toResponse(supplierEntity)).thenReturn(supplierResponse);

        SupplierResponse result = supplierService.update(supplierId, updateRequest);

        assertNotNull(result);
        verify(supplierMapper, times(1)).updateEntityFromDto(updateRequest, supplierEntity);
        verify(supplierRepository, times(1)).save(supplierEntity);
    }

    @Test
    @DisplayName("update - throws 409 conflict when updated email is used by another supplier")
    void update_duplicateEmail_throwsConflict() {
        SupplierUpdateRequest updateRequest = SupplierUpdateRequest.builder()
                .email("other@tannery.it")
                .build();

        when(supplierRepository.findByIdAndIsActiveTrue(supplierId)).thenReturn(Optional.of(supplierEntity));
        when(supplierRepository.existsByEmailIgnoreCaseAndIdNot("other@tannery.it", supplierId)).thenReturn(true);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> supplierService.update(supplierId, updateRequest));

        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
        assertTrue(ex.getReason().contains("already in use"));
        verify(supplierRepository, never()).save(any());
    }

    @Test
    @DisplayName("update - throws 404 when supplier does not exist")
    void update_notFound_throwsException() {
        SupplierUpdateRequest updateRequest = SupplierUpdateRequest.builder()
                .name("New Name")
                .build();

        when(supplierRepository.findByIdAndIsActiveTrue(999L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> supplierService.update(999L, updateRequest));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    @DisplayName("softDeleteById - sets isActive to false")
    void softDeleteById_success() {
        when(supplierRepository.findByIdAndIsActiveTrue(supplierId)).thenReturn(Optional.of(supplierEntity));
        when(supplierRepository.save(supplierEntity)).thenReturn(supplierEntity);
        when(supplierMapper.toResponse(supplierEntity)).thenReturn(supplierResponse);

        SupplierResponse result = supplierService.softDeleteById(supplierId);

        assertNotNull(result);
        assertFalse(supplierEntity.getIsActive());
        verify(supplierRepository, times(1)).save(supplierEntity);
    }

    @Test
    @DisplayName("softDeleteById - throws 404 when supplier not found")
    void softDeleteById_notFound_throwsException() {
        when(supplierRepository.findByIdAndIsActiveTrue(999L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> supplierService.softDeleteById(999L));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    @DisplayName("permanentDeleteById - deletes record from repository")
    void permanentDeleteById_success() {
        when(supplierRepository.findById(supplierId)).thenReturn(Optional.of(supplierEntity));
        doNothing().when(supplierRepository).delete(supplierEntity);

        assertDoesNotThrow(() -> supplierService.permanentDeleteById(supplierId));

        verify(supplierRepository, times(1)).delete(supplierEntity);
    }

    @Test
    @DisplayName("permanentDeleteById - throws 404 when supplier not found")
    void permanentDeleteById_notFound_throwsException() {
        when(supplierRepository.findById(999L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> supplierService.permanentDeleteById(999L));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        verify(supplierRepository, never()).delete(any());
    }
}
