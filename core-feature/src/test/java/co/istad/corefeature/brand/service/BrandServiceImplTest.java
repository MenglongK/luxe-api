package co.istad.corefeature.brand.service;

import co.istad.corefeature.brand.dto.BrandRequest;
import co.istad.corefeature.brand.dto.BrandResponse;
import co.istad.corefeature.brand.entity.BrandEntity;
import co.istad.corefeature.brand.mapper.BrandMapper;
import co.istad.corefeature.brand.mapper.BrandMapperImpl;
import co.istad.corefeature.brand.repository.BrandRepository;
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
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BrandServiceImplTest {

    @Mock
    private BrandRepository brandRepository;

    private BrandMapper brandMapper;
    private BrandService brandService;

    @BeforeEach
    void setUp() {
        brandMapper = new BrandMapperImpl();
        brandService = new BrandServiceImpl(brandRepository, brandMapper);
    }

    @Test
    @DisplayName("Should successfully create a brand")
    void createBrand_success() {
        BrandRequest request = new BrandRequest("Prada", "https://example.com/prada.png", true);

        when(brandRepository.existsByName("Prada")).thenReturn(false);
        when(brandRepository.save(any(BrandEntity.class))).thenAnswer(invocation -> {
            BrandEntity entity = invocation.getArgument(0);
            entity.setId(1L);
            return entity;
        });

        BrandResponse response = brandService.createBrand(request);

        assertNotNull(response);
        assertEquals(1L, response.id());
        assertEquals("Prada", response.name());
        assertEquals("https://example.com/prada.png", response.logoUrl());
        assertTrue(response.isActive());
        verify(brandRepository).save(any(BrandEntity.class));
    }

    @Test
    @DisplayName("Should default isActive to true when creating a brand")
    void createBrand_defaultsIsActiveToTrue() {
        BrandRequest request = new BrandRequest("Gucci", "https://example.com/gucci.png", null);

        when(brandRepository.existsByName("Gucci")).thenReturn(false);
        when(brandRepository.save(any(BrandEntity.class))).thenAnswer(invocation -> {
            BrandEntity entity = invocation.getArgument(0);
            entity.setId(2L);
            return entity;
        });

        BrandResponse response = brandService.createBrand(request);

        assertNotNull(response);
        assertTrue(response.isActive());
    }

    @Test
    @DisplayName("Should throw 409 CONFLICT if brand already exists")
    void createBrand_conflict() {
        BrandRequest request = new BrandRequest("Prada", "https://example.com/prada.png", true);

        when(brandRepository.existsByName("Prada")).thenReturn(true);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
                brandService.createBrand(request)
        );

        assertEquals(409, exception.getStatusCode().value());
        assertTrue(exception.getReason().contains("Brand already exists"));
        verify(brandRepository, never()).save(any(BrandEntity.class));
    }

    @Test
    @DisplayName("Should update existing brand in place without generating a new entity")
    void updateBrand_success() {
        BrandEntity entity = new BrandEntity();
        entity.setId(1L);
        entity.setName("Old Brand");
        entity.setLogoUrl("https://example.com/old.png");
        entity.setIsActive(true);

        BrandRequest request = new BrandRequest("New Brand", "https://example.com/new.png", true);

        when(brandRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(brandRepository.existsByName("New Brand")).thenReturn(false);
        when(brandRepository.save(any(BrandEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BrandResponse response = brandService.updateBrand(request, 1L);

        assertNotNull(response);
        assertEquals(1L, response.id(), "Should retain the same ID on update");
        assertEquals("New Brand", response.name());
        assertEquals("https://example.com/new.png", response.logoUrl());
    }

    @Test
    @DisplayName("Should throw 409 CONFLICT if updated brand name already exists")
    void updateBrand_conflict() {
        BrandEntity entity = new BrandEntity();
        entity.setId(1L);
        entity.setName("Old Brand");
        entity.setIsActive(true);

        BrandRequest request = new BrandRequest("Existing Brand", "https://example.com/logo.png", true);

        when(brandRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(brandRepository.existsByName("Existing Brand")).thenReturn(true);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
                brandService.updateBrand(request, 1L)
        );

        assertEquals(409, exception.getStatusCode().value());
        assertTrue(exception.getReason().contains("Brand name already exists"));
    }

    @Test
    @DisplayName("Should find brand by id")
    void findBrandById_success() {
        BrandEntity entity = new BrandEntity();
        entity.setId(1L);
        entity.setName("Prada");
        entity.setIsActive(true);

        when(brandRepository.findById(1L)).thenReturn(Optional.of(entity));

        BrandResponse response = brandService.findBrandById(1L);

        assertNotNull(response);
        assertEquals(1L, response.id());
        assertEquals("Prada", response.name());
    }

    @Test
    @DisplayName("Should throw 404 NOT_FOUND if brand not found by id")
    void findBrandById_notFound() {
        when(brandRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> brandService.findBrandById(1L));
    }

    @Test
    @DisplayName("Should return page of active brands")
    void findAllBrands_returnsActive() {
        BrandEntity entity = new BrandEntity();
        entity.setId(1L);
        entity.setName("Prada");
        entity.setIsActive(true);

        Pageable pageable = PageRequest.of(0, 10);
        when(brandRepository.findByIsActiveTrue(pageable))
                .thenReturn(new PageImpl<>(List.of(entity), pageable, 1));

        Page<BrandResponse> responses = brandService.findAllBrands(pageable);

        assertEquals(1, responses.getTotalElements());
        assertEquals("Prada", responses.getContent().get(0).name());
    }

    @Test
    @DisplayName("Should soft delete brand")
    void softDeleteById_success() {
        BrandEntity entity = new BrandEntity();
        entity.setId(1L);
        entity.setName("Prada");
        entity.setIsActive(true);

        when(brandRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(brandRepository.save(any(BrandEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BrandResponse response = brandService.softDeleteById(1L);

        assertNotNull(response);
        assertFalse(response.isActive());
    }

    @Test
    @DisplayName("Should permanently delete brand")
    void permanentDeleteById_success() {
        BrandEntity entity = new BrandEntity();
        entity.setId(1L);
        entity.setName("Prada");

        when(brandRepository.findById(1L)).thenReturn(Optional.of(entity));

        brandService.permanentDeleteById(1L);

        verify(brandRepository).delete(entity);
    }
}
