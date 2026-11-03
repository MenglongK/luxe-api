package co.istad.corefeature.facility.mapper;

import co.istad.corefeature.common.enums.FacilityType;
import co.istad.corefeature.facility.dto.AddressRequest;
import co.istad.corefeature.facility.dto.AddressResponse;
import co.istad.corefeature.facility.dto.FacilityRequest;
import co.istad.corefeature.facility.dto.FacilityResponse;
import co.istad.corefeature.facility.entity.Address;
import co.istad.corefeature.facility.entity.FacilityEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FacilityMapperTest {

    private FacilityMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new FacilityMapperImpl();
    }

    @Test
    @DisplayName("Should map FacilityRequest with AddressRequest to FacilityEntity")
    void toEntity_withAddress() {
        AddressRequest addressRequest = AddressRequest.builder()
                .addressLine1("Street 2004, Building 5")
                .addressLine2("Unit 3A")
                .city("Phnom Penh")
                .province("Phnom Penh")
                .postalCode("12000")
                .country("Cambodia")
                .build();

        FacilityRequest request = FacilityRequest.builder()
                .code("FAC-001")
                .name("Main Distribution Hub")
                .type(FacilityType.WAREHOUSE)
                .address(addressRequest)
                .isActive(true)
                .build();

        FacilityEntity entity = mapper.toEntity(request);

        assertNotNull(entity);
        assertEquals("FAC-001", entity.getCode());
        assertEquals("Main Distribution Hub", entity.getName());
        assertEquals(FacilityType.WAREHOUSE, entity.getType());
        assertTrue(entity.getIsActive());

        Address address = entity.getAddress();
        assertNotNull(address);
        assertEquals("Street 2004, Building 5", address.getAddressLine1());
        assertEquals("Unit 3A", address.getAddressLine2());
        assertEquals("Phnom Penh", address.getCity());
        assertEquals("Phnom Penh", address.getProvince());
        assertEquals("12000", address.getPostalCode());
        assertEquals("Cambodia", address.getCountry());
    }

    @Test
    @DisplayName("Should map FacilityEntity to FacilityResponse with AddressResponse")
    void toResponse_withAddress() {
        Address address = Address.builder()
                .addressLine1("Street 2004, Building 5")
                .addressLine2(null)
                .city("Phnom Penh")
                .province("Phnom Penh")
                .postalCode("12000")
                .country("Cambodia")
                .build();

        FacilityEntity entity = FacilityEntity.builder()
                .id(1L)
                .code("FAC-001")
                .name("Main Distribution Hub")
                .type(FacilityType.BRANCH)
                .address(address)
                .isActive(true)
                .build();

        FacilityResponse response = mapper.toResponse(entity);

        assertNotNull(response);
        assertEquals(1L, response.id());
        assertEquals("FAC-001", response.code());
        assertEquals("Main Distribution Hub", response.name());
        assertEquals(FacilityType.BRANCH, response.type());
        assertTrue(response.isActive());

        AddressResponse addressResponse = response.address();
        assertNotNull(addressResponse);
        assertEquals("Street 2004, Building 5", addressResponse.addressLine1());
        assertNull(addressResponse.addressLine2());
        assertEquals("Phnom Penh", addressResponse.city());
        assertEquals("Phnom Penh", addressResponse.province());
        assertEquals("12000", addressResponse.postalCode());
        assertEquals("Cambodia", addressResponse.country());
    }

    @Test
    @DisplayName("Should update entity and address from request")
    void updateEntityFromRequest() {
        Address oldAddress = Address.builder()
                .addressLine1("Old Street 1")
                .addressLine2("Old Floor 1")
                .city("Old City")
                .province("Old Province")
                .postalCode("11111")
                .country("Old Country")
                .build();

        FacilityEntity entity = FacilityEntity.builder()
                .id(1L)
                .code("OLD-01")
                .name("Old Name")
                .type(FacilityType.WAREHOUSE)
                .address(oldAddress)
                .isActive(true)
                .build();

        AddressRequest newAddressRequest = AddressRequest.builder()
                .addressLine1("New Street 2")
                .addressLine2(null)
                .city("New City")
                .province("New Province")
                .postalCode("22222")
                .country("New Country")
                .build();

        FacilityRequest request = FacilityRequest.builder()
                .code("NEW-01")
                .name("New Name")
                .type(FacilityType.BRANCH)
                .address(newAddressRequest)
                .isActive(false)
                .build();

        mapper.updateEntityFromRequest(request, entity);

        assertEquals("NEW-01", entity.getCode());
        assertEquals("New Name", entity.getName());
        assertEquals(FacilityType.BRANCH, entity.getType());
        assertFalse(entity.getIsActive());

        assertNotNull(entity.getAddress());
        assertEquals("New Street 2", entity.getAddress().getAddressLine1());
        assertNull(entity.getAddress().getAddressLine2());
        assertEquals("New City", entity.getAddress().getCity());
        assertEquals("New Province", entity.getAddress().getProvince());
        assertEquals("22222", entity.getAddress().getPostalCode());
        assertEquals("New Country", entity.getAddress().getCountry());
    }

    @Test
    @DisplayName("Should handle null address in request and entity")
    void nullAddressHandling() {
        FacilityRequest requestWithoutAddress = FacilityRequest.builder()
                .code("NO-ADDR")
                .name("No Address Facility")
                .type(FacilityType.WAREHOUSE)
                .address(null)
                .isActive(true)
                .build();

        FacilityEntity entity = mapper.toEntity(requestWithoutAddress);
        assertNotNull(entity);
        assertNull(entity.getAddress());

        FacilityResponse response = mapper.toResponse(entity);
        assertNotNull(response);
        assertNull(response.address());

        assertNull(mapper.toAddress(null));
        assertNull(mapper.toAddressResponse(null));
    }
}
