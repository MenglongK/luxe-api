package co.istad.corefeature.facility.service;

import co.istad.corefeature.common.enums.FacilityType;
import co.istad.corefeature.facility.dto.AddressRequest;
import co.istad.corefeature.facility.dto.FacilityRequest;
import co.istad.corefeature.facility.dto.FacilityResponse;
import co.istad.corefeature.facility.entity.Address;
import co.istad.corefeature.facility.entity.FacilityEntity;
import co.istad.corefeature.facility.mapper.FacilityMapper;
import co.istad.corefeature.facility.mapper.FacilityMapperImpl;
import co.istad.corefeature.facility.repository.FacilityRepository;
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
class FacilityServiceImplTest {

    @Mock
    private FacilityRepository facilityRepository;

    private FacilityMapper facilityMapper;
    private FacilityService facilityService;

    @BeforeEach
    void setUp() {
        facilityMapper = new FacilityMapperImpl();
        facilityService = new FacilityServiceImpl(facilityRepository, facilityMapper);
    }

    @Test
    @DisplayName("Should successfully create a facility with address")
    void createFacility_withAddress() {
        AddressRequest addressRequest = AddressRequest.builder()
                .addressLine1("123 Test St")
                .addressLine2("Suite 100")
                .city("Phnom Penh")
                .province("Phnom Penh")
                .postalCode("12000")
                .country("Cambodia")
                .build();

        FacilityRequest request = FacilityRequest.builder()
                .code("FAC-01")
                .name("Facility 1")
                .type(FacilityType.WAREHOUSE)
                .address(addressRequest)
                .isActive(true)
                .build();

        when(facilityRepository.existsByCode("FAC-01")).thenReturn(false);
        when(facilityRepository.save(any(FacilityEntity.class))).thenAnswer(invocation -> {
            FacilityEntity saved = invocation.getArgument(0);
            saved.setId(1L);
            return saved;
        });

        FacilityResponse response = facilityService.create(request);

        assertNotNull(response);
        assertEquals(1L, response.id());
        assertEquals("FAC-01", response.code());
        assertNotNull(response.address());
        assertEquals("123 Test St", response.address().addressLine1());
        assertEquals("Suite 100", response.address().addressLine2());
        assertEquals("Phnom Penh", response.address().city());
        assertEquals("Phnom Penh", response.address().province());
        assertEquals("12000", response.address().postalCode());
        assertEquals("Cambodia", response.address().country());

        verify(facilityRepository).save(any(FacilityEntity.class));
    }

    @Test
    @DisplayName("Should find facility by id with address")
    void findById_withAddress() {
        Address address = Address.builder()
                .addressLine1("123 Test St")
                .city("Phnom Penh")
                .province("Phnom Penh")
                .postalCode("12000")
                .country("Cambodia")
                .build();

        FacilityEntity entity = FacilityEntity.builder()
                .id(1L)
                .code("FAC-01")
                .name("Facility 1")
                .type(FacilityType.BRANCH)
                .address(address)
                .isActive(true)
                .build();

        when(facilityRepository.findById(1L)).thenReturn(Optional.of(entity));

        FacilityResponse response = facilityService.findById(1L);

        assertNotNull(response);
        assertEquals(1L, response.id());
        assertNotNull(response.address());
        assertEquals("123 Test St", response.address().addressLine1());
        assertNull(response.address().addressLine2());
    }

    @Test
    @DisplayName("Should update facility and address")
    void updateFacility_withAddress() {
        Address oldAddress = Address.builder()
                .addressLine1("Old St")
                .city("Old City")
                .province("Old Province")
                .postalCode("00000")
                .country("Old Country")
                .build();

        FacilityEntity entity = FacilityEntity.builder()
                .id(1L)
                .code("FAC-01")
                .name("Facility 1")
                .type(FacilityType.BRANCH)
                .address(oldAddress)
                .isActive(true)
                .build();

        AddressRequest updatedAddress = AddressRequest.builder()
                .addressLine1("Updated St")
                .addressLine2("Unit 5")
                .city("Updated City")
                .province("Updated Province")
                .postalCode("99999")
                .country("Updated Country")
                .build();

        FacilityRequest updateRequest = FacilityRequest.builder()
                .code("FAC-01")
                .name("Facility 1 Updated")
                .type(FacilityType.WAREHOUSE)
                .address(updatedAddress)
                .isActive(true)
                .build();

        when(facilityRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(facilityRepository.save(any(FacilityEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        FacilityResponse response = facilityService.update(1L, updateRequest);

        assertNotNull(response);
        assertEquals("Facility 1 Updated", response.name());
        assertEquals(FacilityType.WAREHOUSE, response.type());
        assertNotNull(response.address());
        assertEquals("Updated St", response.address().addressLine1());
        assertEquals("Unit 5", response.address().addressLine2());
        assertEquals("Updated City", response.address().city());
    }

    @Test
    @DisplayName("Should find all active facilities with addresses")
    void findAll_returnsActiveFacilities() {
        Address address = Address.builder()
                .addressLine1("Street 1")
                .city("City")
                .province("Province")
                .postalCode("12345")
                .country("Country")
                .build();

        FacilityEntity entity = FacilityEntity.builder()
                .id(1L)
                .code("FAC-01")
                .name("Facility 1")
                .type(FacilityType.BRANCH)
                .address(address)
                .isActive(true)
                .build();

        Pageable pageable = PageRequest.of(0, 10);
        when(facilityRepository.findByIsActiveTrue(pageable)).thenReturn(new PageImpl<>(List.of(entity), pageable, 1));

        Page<FacilityResponse> responses = facilityService.findAll(pageable);

        assertEquals(1, responses.getTotalElements());
        assertEquals(1, responses.getContent().size());
        assertNotNull(responses.getContent().getFirst().address());
        assertEquals("Street 1", responses.getContent().getFirst().address().addressLine1());
    }
}
