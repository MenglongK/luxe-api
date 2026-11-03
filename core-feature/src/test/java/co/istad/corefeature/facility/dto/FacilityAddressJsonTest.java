package co.istad.corefeature.facility.dto;

import co.istad.corefeature.common.enums.FacilityType;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class FacilityAddressJsonTest {

    private ObjectMapper objectMapper;
    private Validator validator;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    @DisplayName("Should deserialize FacilityRequest with camelCase address fields")
    void deserialize_camelCaseAddress() throws Exception {
        String json = """
                {
                    "code": "FAC-01",
                    "name": "Central Hub",
                    "type": "WAREHOUSE",
                    "address": {
                        "addressLine1": "123 Main St",
                        "addressLine2": "Suite 400",
                        "city": "Phnom Penh",
                        "province": "Phnom Penh",
                        "postalCode": "12000",
                        "country": "Cambodia"
                    },
                    "isActive": true
                }
                """;

        FacilityRequest request = objectMapper.readValue(json, FacilityRequest.class);

        assertEquals("FAC-01", request.code());
        assertEquals("Central Hub", request.name());
        assertEquals(FacilityType.WAREHOUSE, request.type());
        assertTrue(request.isActive());

        assertNotNull(request.address());
        assertEquals("123 Main St", request.address().addressLine1());
        assertEquals("Suite 400", request.address().addressLine2());
        assertEquals("Phnom Penh", request.address().city());
        assertEquals("Phnom Penh", request.address().province());
        assertEquals("12000", request.address().postalCode());
        assertEquals("Cambodia", request.address().country());

        Set<ConstraintViolation<FacilityRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty(), "Should have no validation violations");
    }

    @Test
    @DisplayName("Should deserialize FacilityRequest with snake_case address aliases")
    void deserialize_snakeCaseAddressAliases() throws Exception {
        String json = """
                {
                    "code": "FAC-02",
                    "name": "Branch North",
                    "type": "BRANCH",
                    "address": {
                        "address_line_1": "456 North Blvd",
                        "address_line_2": "Floor 2",
                        "city": "Siem Reap",
                        "province": "Siem Reap",
                        "postal_code": "17000",
                        "country": "Cambodia"
                    },
                    "isActive": true
                }
                """;

        FacilityRequest request = objectMapper.readValue(json, FacilityRequest.class);

        assertNotNull(request.address());
        assertEquals("456 North Blvd", request.address().addressLine1());
        assertEquals("Floor 2", request.address().addressLine2());
        assertEquals("Siem Reap", request.address().city());
        assertEquals("Siem Reap", request.address().province());
        assertEquals("17000", request.address().postalCode());
        assertEquals("Cambodia", request.address().country());

        Set<ConstraintViolation<FacilityRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty());
    }

    @Test
    @DisplayName("Should permit optional addressLine2 to be omitted or null")
    void addressLine2_isOptional() throws Exception {
        String json = """
                {
                    "code": "FAC-03",
                    "name": "South Warehouse",
                    "type": "WAREHOUSE",
                    "address": {
                        "addressLine1": "789 South Ave",
                        "city": "Battambang",
                        "province": "Battambang",
                        "postalCode": "02000",
                        "country": "Cambodia"
                    }
                }
                """;

        FacilityRequest request = objectMapper.readValue(json, FacilityRequest.class);

        assertNotNull(request.address());
        assertEquals("789 South Ave", request.address().addressLine1());
        assertNull(request.address().addressLine2());
        assertEquals("Battambang", request.address().city());
        assertEquals("Battambang", request.address().province());
        assertEquals("02000", request.address().postalCode());
        assertEquals("Cambodia", request.address().country());

        Set<ConstraintViolation<FacilityRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty(), "addressLine2 should be optional");
    }

    @Test
    @DisplayName("Should fail validation when mandatory address fields are blank")
    void validate_mandatoryAddressFields() {
        AddressRequest invalidAddress = AddressRequest.builder()
                .addressLine1("")
                .city(" ")
                .province("")
                .postalCode("")
                .country("")
                .build();

        FacilityRequest request = FacilityRequest.builder()
                .code("FAC-INV")
                .name("Invalid Facility")
                .type(FacilityType.BRANCH)
                .address(invalidAddress)
                .build();

        Set<ConstraintViolation<FacilityRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
        assertEquals(5, violations.size(), "Should violate 5 mandatory fields in address");
    }

    @Test
    @DisplayName("Should serialize FacilityResponse with address object")
    void serialize_facilityResponse() throws Exception {
        AddressResponse addressResponse = AddressResponse.builder()
                .addressLine1("123 Main St")
                .addressLine2("Apt 4B")
                .city("Phnom Penh")
                .province("Phnom Penh")
                .postalCode("12000")
                .country("Cambodia")
                .build();

        FacilityResponse response = FacilityResponse.builder()
                .id(10L)
                .code("FAC-10")
                .name("HQ Facility")
                .type(FacilityType.BRANCH)
                .address(addressResponse)
                .isActive(true)
                .build();

        String json = objectMapper.writeValueAsString(response);

        assertTrue(json.contains("\"addressLine1\":\"123 Main St\""));
        assertTrue(json.contains("\"addressLine2\":\"Apt 4B\""));
        assertTrue(json.contains("\"city\":\"Phnom Penh\""));
        assertTrue(json.contains("\"province\":\"Phnom Penh\""));
        assertTrue(json.contains("\"postalCode\":\"12000\""));
        assertTrue(json.contains("\"country\":\"Cambodia\""));
    }
}
