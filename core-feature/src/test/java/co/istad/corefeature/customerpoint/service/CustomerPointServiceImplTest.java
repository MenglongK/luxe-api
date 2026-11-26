package co.istad.corefeature.customerpoint.service;

import co.istad.corefeature.common.enums.CustomerTier;
import co.istad.corefeature.common.enums.PointTransactionType;
import co.istad.corefeature.customerpoint.dto.CustomerPointRequest;
import co.istad.corefeature.customerpoint.dto.CustomerPointResponse;
import co.istad.corefeature.customerpoint.dto.PointsRequest;
import co.istad.corefeature.customerpoint.entity.CustomerPointEntity;
import co.istad.corefeature.customerpoint.entity.CustomerPointTransactionEntity;
import co.istad.corefeature.customerpoint.mapper.CustomerPointMapper;
import co.istad.corefeature.customerpoint.mapper.CustomerPointTransactionMapper;
import co.istad.corefeature.customerpoint.repository.CustomerPointRepository;
import co.istad.corefeature.customerpoint.repository.CustomerPointTransactionRepository;
import co.istad.corefeature.user.customer.entity.CustomerEntity;
import co.istad.corefeature.user.customer.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerPointServiceImplTest {

    @Mock
    private CustomerPointRepository customerPointRepository;

    @Mock
    private CustomerPointTransactionRepository customerPointTransactionRepository;

    @Mock
    private CustomerRepository customerRepository;

    private final CustomerPointMapper customerPointMapper = Mappers.getMapper(CustomerPointMapper.class);
    private final CustomerPointTransactionMapper transactionMapper = Mappers.getMapper(CustomerPointTransactionMapper.class);

    private CustomerPointService customerPointService;

    @BeforeEach
    void setUp() {
        customerPointService = new CustomerPointServiceImpl(
                customerPointRepository,
                customerPointTransactionRepository,
                customerRepository,
                customerPointMapper,
                transactionMapper
        );
    }

    @Test
    @DisplayName("Should create membership point account and auto-upgrade customer tier")
    void create_autoUpgradesCustomerTier() {
        CustomerEntity customer = CustomerEntity.builder()
                .customerCode("cust-001")
                .tier(CustomerTier.STANDARD)
                .isActive(true)
                .build();

        when(customerPointRepository.existsByCustomerId("cust-001")).thenReturn(false);
        when(customerRepository.findByCustomerCodeAndIsActiveTrue("cust-001")).thenReturn(Optional.of(customer));
        when(customerPointRepository.save(any(CustomerPointEntity.class))).thenAnswer(inv -> {
            CustomerPointEntity entity = inv.getArgument(0);
            entity.setId(1L);
            return entity;
        });

        CustomerPointResponse response = customerPointService.create(
                CustomerPointRequest.builder().customerId("cust-001").totalPoints(1500).build()
        );

        assertNotNull(response);
        assertTrue(response.membershipNumber().startsWith("MEM-"));
        assertEquals(1500, response.totalPoints());
        assertEquals(1500, response.lifetimePoints());
        assertEquals(CustomerTier.SILVER, response.tier());
        assertEquals(CustomerTier.GOLD_ATELIER, response.nextTier());
        assertEquals(1500, response.pointsToNextTier());
        assertEquals(CustomerTier.SILVER, customer.getTier());

        verify(customerRepository, times(1)).save(customer);
        verify(customerPointTransactionRepository, times(1)).save(any(CustomerPointTransactionEntity.class));
    }

    @Test
    @DisplayName("Should throw 409 CONFLICT when creating duplicate membership account")
    void create_duplicateCustomer_throwsConflict() {
        when(customerPointRepository.existsByCustomerId("cust-001")).thenReturn(true);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                customerPointService.create(CustomerPointRequest.builder().customerId("cust-001").totalPoints(100).build())
        );

        assertEquals(409, ex.getStatusCode().value());
        verify(customerPointRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should auto-upgrade tier from Silver to Gold Atelier and VIP Royal when earning points")
    void earn_autoUpgradesThroughTiers() {
        CustomerEntity customer = CustomerEntity.builder()
                .customerCode("cust-002")
                .tier(CustomerTier.SILVER)
                .isActive(true)
                .build();

        CustomerPointEntity account = CustomerPointEntity.builder()
                .id(10L)
                .membershipNumber("MEM-LUXE-0002")
                .customerId("cust-002")
                .totalPoints(1200)
                .lifetimePoints(1200)
                .tier(CustomerTier.SILVER)
                .isActive(true)
                .build();

        when(customerPointRepository.findByCustomerIdAndIsActiveTrue("cust-002")).thenReturn(Optional.of(account));
        when(customerRepository.findByCustomerCodeAndIsActiveTrue("cust-002")).thenReturn(Optional.of(customer));
        when(customerPointRepository.save(any(CustomerPointEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        // 1200 + 2000 = 3200 -> GOLD_ATELIER
        CustomerPointResponse goldResponse = customerPointService.earnByCustomerId("cust-002", new PointsRequest(2000));
        assertEquals(3200, goldResponse.totalPoints());
        assertEquals(3200, goldResponse.lifetimePoints());
        assertEquals(CustomerTier.GOLD_ATELIER, goldResponse.tier());
        assertEquals(CustomerTier.VIP_ROYAL, goldResponse.nextTier());
        assertEquals(3800, goldResponse.pointsToNextTier());
        assertEquals(CustomerTier.GOLD_ATELIER, customer.getTier());

        // 3200 + 4000 = 7200 -> VIP_ROYAL
        CustomerPointResponse vipResponse = customerPointService.earnByCustomerId("cust-002", new PointsRequest(4000));
        assertEquals(7200, vipResponse.totalPoints());
        assertEquals(7200, vipResponse.lifetimePoints());
        assertEquals(CustomerTier.VIP_ROYAL, vipResponse.tier());
        assertNull(vipResponse.nextTier());
        assertEquals(0, vipResponse.pointsToNextTier());
        assertEquals(CustomerTier.VIP_ROYAL, customer.getTier());
    }

    @Test
    @DisplayName("Should redeem spendable points without downgrading lifetime points or membership tier")
    void redeem_preservesLifetimePointsAndTier() {
        CustomerPointEntity account = CustomerPointEntity.builder()
                .id(11L)
                .membershipNumber("MEM-LUXE-0003")
                .customerId("cust-003")
                .totalPoints(3500)
                .lifetimePoints(3500)
                .tier(CustomerTier.GOLD_ATELIER)
                .isActive(true)
                .build();

        when(customerPointRepository.findByIdAndIsActiveTrue(11L)).thenReturn(Optional.of(account));
        when(customerPointRepository.save(any(CustomerPointEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        CustomerPointResponse response = customerPointService.redeem(11L, new PointsRequest(3000));

        assertEquals(500, response.totalPoints());
        assertEquals(3500, response.lifetimePoints());
        assertEquals(CustomerTier.GOLD_ATELIER, response.tier());

        ArgumentCaptor<CustomerPointTransactionEntity> txCaptor = ArgumentCaptor.forClass(CustomerPointTransactionEntity.class);
        verify(customerPointTransactionRepository).save(txCaptor.capture());
        assertEquals(PointTransactionType.REDEEMED, txCaptor.getValue().getTransactionType());
        assertEquals(500, txCaptor.getValue().getBalanceAfter());
    }

    @Test
    @DisplayName("Should throw 400 BAD_REQUEST when redeeming more points than available")
    void redeem_insufficientPoints_throwsBadRequest() {
        CustomerPointEntity account = CustomerPointEntity.builder()
                .id(12L)
                .membershipNumber("MEM-LUXE-0004")
                .customerId("cust-004")
                .totalPoints(200)
                .lifetimePoints(200)
                .tier(CustomerTier.STANDARD)
                .isActive(true)
                .build();

        when(customerPointRepository.findByIdAndIsActiveTrue(12L)).thenReturn(Optional.of(account));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                customerPointService.redeem(12L, new PointsRequest(500))
        );

        assertEquals(400, ex.getStatusCode().value());
        verify(customerPointRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should never downgrade a customer who already holds a higher tier")
    void earn_neverDowngradesHigherCustomerTier() {
        CustomerEntity customer = CustomerEntity.builder()
                .customerCode("cust-vip")
                .tier(CustomerTier.VIP_ROYAL)
                .isActive(true)
                .build();

        CustomerPointEntity account = CustomerPointEntity.builder()
                .id(13L)
                .membershipNumber("MEM-LUXE-0005")
                .customerId("cust-vip")
                .totalPoints(100)
                .lifetimePoints(100)
                .tier(CustomerTier.VIP_ROYAL)
                .isActive(true)
                .build();

        when(customerPointRepository.findByIdAndIsActiveTrue(13L)).thenReturn(Optional.of(account));
        when(customerRepository.findByCustomerCodeAndIsActiveTrue("cust-vip")).thenReturn(Optional.of(customer));
        when(customerPointRepository.save(any(CustomerPointEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        CustomerPointResponse response = customerPointService.earn(13L, new PointsRequest(200));

        assertEquals(300, response.totalPoints());
        assertEquals(CustomerTier.VIP_ROYAL, response.tier());
        assertEquals(CustomerTier.VIP_ROYAL, customer.getTier());
    }

    @Test
    @DisplayName("Should retrieve membership account by membership number")
    void findByMembershipNumber_success() {
        CustomerPointEntity account = CustomerPointEntity.builder()
                .id(14L)
                .membershipNumber("MEM-LUXE-0014")
                .customerId("cust-014")
                .totalPoints(1000)
                .lifetimePoints(1000)
                .tier(CustomerTier.SILVER)
                .isActive(true)
                .build();

        when(customerPointRepository.findByMembershipNumberAndIsActiveTrue("MEM-LUXE-0014"))
                .thenReturn(Optional.of(account));

        CustomerPointResponse response = customerPointService.findByMembershipNumber("MEM-LUXE-0014");

        assertEquals("MEM-LUXE-0014", response.membershipNumber());
        assertEquals(CustomerTier.SILVER, response.tier());
    }
}
