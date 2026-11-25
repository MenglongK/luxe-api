package co.istad.corefeature.user.customer.service;

import co.istad.corefeature.common.enums.CustomerTier;
import co.istad.corefeature.common.enums.Gender;
import co.istad.corefeature.customerpoint.entity.CustomerPointEntity;
import co.istad.corefeature.customerpoint.repository.CustomerPointRepository;
import co.istad.corefeature.facility.dto.AddressRequest;
import co.istad.corefeature.facility.dto.AddressResponse;
import co.istad.corefeature.facility.entity.Address;
import co.istad.corefeature.order.entity.OrderEntity;
import co.istad.corefeature.order.repository.OrderRepository;
import co.istad.corefeature.user.customer.dto.CustomerRequest;
import co.istad.corefeature.user.customer.dto.CustomerResponse;
import co.istad.corefeature.user.customer.dto.CustomerSummaryResponse;
import co.istad.corefeature.user.customer.dto.CustomerUpdateRequest;
import co.istad.corefeature.user.customer.entity.CustomerEntity;
import co.istad.corefeature.user.customer.mapper.CustomerMapper;
import co.istad.corefeature.user.customer.repository.CustomerRepository;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerServiceImplTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private CustomerPointRepository customerPointRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CustomerMapper customerMapper;

    private CustomerService customerService;

    private UUID customerId;
    private CustomerEntity customerEntity;
    private CustomerRequest customerRequest;
    private CustomerResponse customerResponse;

    @BeforeEach
    void setUp() {
        customerService = new CustomerServiceImpl(
                customerRepository,
                customerPointRepository,
                orderRepository,
                customerMapper
        );

        customerId = UUID.randomUUID();

        Address address = Address.builder()
                .addressLine1("No. 128 Norodom Blvd")
                .city("Phnom Penh")
                .province("Phnom Penh")
                .postalCode("12000")
                .country("Cambodia")
                .build();

        AddressResponse addressResponse = AddressResponse.builder()
                .addressLine1("No. 128 Norodom Blvd")
                .city("Phnom Penh")
                .province("Phnom Penh")
                .postalCode("12000")
                .country("Cambodia")
                .build();

        customerEntity = CustomerEntity.builder()
                .id(customerId)
                .customerCode("cust-vip-001")
                .name("Lady Genevieve Vance")
                .email("genevieve.vance@atelier-club.com")
                .phone("+855 12 889 001")
                .avatarUrl("https://example.com/avatar.jpg")
                .tier(CustomerTier.VIP_ROYAL)
                .gender(Gender.WOMEN)
                .primaryAddress(address)
                .bespokeNotes("Prefers Vicuna knitwear.")
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        customerRequest = CustomerRequest.builder()
                .name("Lady Genevieve Vance")
                .email("genevieve.vance@atelier-club.com")
                .phone("+855 12 889 001")
                .customerCode("cust-vip-001")
                .avatarUrl("https://example.com/avatar.jpg")
                .tier(CustomerTier.VIP_ROYAL)
                .gender(Gender.WOMEN)
                .bespokeNotes("Prefers Vicuna knitwear.")
                .build();

        customerResponse = CustomerResponse.builder()
                .id(customerId)
                .customerCode("cust-vip-001")
                .name("Lady Genevieve Vance")
                .email("genevieve.vance@atelier-club.com")
                .phone("+855 12 889 001")
                .avatarUrl("https://example.com/avatar.jpg")
                .tier(CustomerTier.VIP_ROYAL)
                .gender(Gender.WOMEN)
                .primaryAddress(addressResponse)
                .bespokeNotes("Prefers Vicuna knitwear.")
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Should successfully create a customer")
    void createCustomer_success() {
        when(customerRepository.existsByEmail("genevieve.vance@atelier-club.com")).thenReturn(false);
        when(customerRepository.existsByCustomerCode("cust-vip-001")).thenReturn(false);
        when(customerMapper.toEntity(customerRequest)).thenReturn(customerEntity);
        when(customerRepository.save(customerEntity)).thenReturn(customerEntity);
        when(customerMapper.toResponse(customerEntity)).thenReturn(customerResponse);

        CustomerResponse response = customerService.create(customerRequest);

        assertNotNull(response);
        assertEquals("cust-vip-001", response.customerCode());
        assertEquals("genevieve.vance@atelier-club.com", response.email());
        assertEquals(CustomerTier.VIP_ROYAL, response.tier());
        verify(customerRepository, times(1)).save(customerEntity);
    }

    @Test
    @DisplayName("Should throw 409 CONFLICT when email already exists")
    void createCustomer_duplicateEmail_throwsConflict() {
        when(customerRepository.existsByEmail("genevieve.vance@atelier-club.com")).thenReturn(true);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> customerService.create(customerRequest));

        assertEquals(409, exception.getStatusCode().value());
        assertTrue(exception.getReason().contains("already exists"));
        verify(customerRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw 409 CONFLICT when customer code already exists")
    void createCustomer_duplicateCode_throwsConflict() {
        when(customerRepository.existsByEmail("genevieve.vance@atelier-club.com")).thenReturn(false);
        when(customerRepository.existsByCustomerCode("cust-vip-001")).thenReturn(true);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> customerService.create(customerRequest));

        assertEquals(409, exception.getStatusCode().value());
        assertTrue(exception.getReason().contains("Customer code"));
        verify(customerRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should retrieve active customer by ID")
    void findById_success() {
        when(customerRepository.findByIdAndIsActiveTrue(customerId)).thenReturn(Optional.of(customerEntity));
        when(customerMapper.toResponse(customerEntity)).thenReturn(customerResponse);

        CustomerResponse response = customerService.findById(customerId);

        assertNotNull(response);
        assertEquals(customerId, response.id());
        assertEquals("Lady Genevieve Vance", response.name());
    }

    @Test
    @DisplayName("Should throw 404 NOT_FOUND when customer ID does not exist")
    void findById_notFound() {
        when(customerRepository.findByIdAndIsActiveTrue(customerId)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> customerService.findById(customerId));
    }

    @Test
    @DisplayName("Should retrieve active customer by customer code")
    void findByCustomerCode_success() {
        when(customerRepository.findByCustomerCodeAndIsActiveTrue("cust-vip-001")).thenReturn(Optional.of(customerEntity));
        when(customerMapper.toResponse(customerEntity)).thenReturn(customerResponse);

        CustomerResponse response = customerService.findByCustomerCode("cust-vip-001");

        assertNotNull(response);
        assertEquals("cust-vip-001", response.customerCode());
    }

    @Test
    @DisplayName("Should retrieve active customer by email")
    void findByEmail_success() {
        when(customerRepository.findByEmailAndIsActiveTrue("genevieve.vance@atelier-club.com")).thenReturn(Optional.of(customerEntity));
        when(customerMapper.toResponse(customerEntity)).thenReturn(customerResponse);

        CustomerResponse response = customerService.findByEmail("genevieve.vance@atelier-club.com");

        assertNotNull(response);
        assertEquals("genevieve.vance@atelier-club.com", response.email());
    }

    @Test
    @DisplayName("Should successfully update customer profile")
    void updateCustomer_success() {
        CustomerUpdateRequest updateRequest = CustomerUpdateRequest.builder()
                .name("Lady Genevieve Vance - Duchess")
                .phone("+855 12 999 999")
                .tier(CustomerTier.VIP_ROYAL)
                .bespokeNotes("Updated luxury bespoke measurements.")
                .build();

        when(customerRepository.findByIdAndIsActiveTrue(customerId)).thenReturn(Optional.of(customerEntity));
        when(customerRepository.save(customerEntity)).thenReturn(customerEntity);
        when(customerMapper.toResponse(customerEntity)).thenReturn(customerResponse);

        CustomerResponse response = customerService.update(customerId, updateRequest);

        assertNotNull(response);
        verify(customerMapper, times(1)).updateEntityFromDto(updateRequest, customerEntity);
        verify(customerRepository, times(1)).save(customerEntity);
    }

    @Test
    @DisplayName("Should throw 409 CONFLICT if updating email to another customer's email")
    void updateCustomer_emailConflict() {
        CustomerUpdateRequest updateRequest = CustomerUpdateRequest.builder()
                .email("other.patron@atelier-club.com")
                .build();

        when(customerRepository.findByIdAndIsActiveTrue(customerId)).thenReturn(Optional.of(customerEntity));
        when(customerRepository.existsByEmailAndIdNot("other.patron@atelier-club.com", customerId)).thenReturn(true);

        assertThrows(ResponseStatusException.class, () -> customerService.update(customerId, updateRequest));
        verify(customerRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should soft delete customer by setting isActive to false")
    void softDeleteById_success() {
        when(customerRepository.findByIdAndIsActiveTrue(customerId)).thenReturn(Optional.of(customerEntity));
        when(customerRepository.save(customerEntity)).thenReturn(customerEntity);
        when(customerMapper.toResponse(customerEntity)).thenReturn(customerResponse);

        CustomerResponse response = customerService.softDeleteById(customerId);

        assertNotNull(response);
        assertFalse(customerEntity.getIsActive());
        verify(customerRepository, times(1)).save(customerEntity);
    }

    @Test
    @DisplayName("Should hard delete customer permanently")
    void permanentDeleteById_success() {
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customerEntity));

        customerService.permanentDeleteById(customerId);

        verify(customerRepository, times(1)).delete(customerEntity);
    }

    @Test
    @DisplayName("Should retrieve customer summary with calculated metrics")
    void getSummary_success() {
        when(customerRepository.findByIdAndIsActiveTrue(customerId)).thenReturn(Optional.of(customerEntity));

        CustomerPointEntity pointEntity = CustomerPointEntity.builder()
                .customerId("cust-vip-001")
                .totalPoints(8500)
                .build();
        when(customerPointRepository.findByCustomerId("cust-vip-001")).thenReturn(Optional.of(pointEntity));

        OrderEntity order1 = OrderEntity.builder()
                .grandTotal(BigDecimal.valueOf(4500.00))
                .createdAt(LocalDateTime.now().minusDays(10))
                .build();
        OrderEntity order2 = OrderEntity.builder()
                .grandTotal(BigDecimal.valueOf(3200.00))
                .createdAt(LocalDateTime.now().minusDays(2))
                .build();

        when(orderRepository.findByCustomerIdAndIsActiveTrue("cust-vip-001")).thenReturn(List.of(order1, order2));

        CustomerSummaryResponse summary = customerService.getSummary(customerId);

        assertNotNull(summary);
        assertEquals(customerId, summary.id());
        assertEquals("cust-vip-001", summary.customerCode());
        assertEquals(8500, summary.totalPoints());
        assertEquals(2L, summary.totalOrders());
        assertEquals(BigDecimal.valueOf(7700.00), summary.totalSpent());
        assertNotNull(summary.lastOrderDate());
    }

    @Test
    @DisplayName("Should retrieve paginated customers with filters")
    void findAll_withFilters() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<CustomerEntity> page = new PageImpl<>(List.of(customerEntity), pageable, 1);

        when(customerRepository.findWithFilters(CustomerTier.VIP_ROYAL, "Genevieve", pageable)).thenReturn(page);
        when(customerMapper.toResponse(customerEntity)).thenReturn(customerResponse);

        Page<CustomerResponse> result = customerService.findAll(CustomerTier.VIP_ROYAL, "Genevieve", pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("Lady Genevieve Vance", result.getContent().get(0).name());
    }
}
