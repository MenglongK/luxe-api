package co.istad.corefeature.order.service;

import co.istad.corefeature.cart.service.CartService;
import co.istad.corefeature.common.enums.*;
import co.istad.corefeature.facility.dto.AddressRequest;
import co.istad.corefeature.facility.entity.Address;
import co.istad.corefeature.facility.entity.FacilityEntity;
import co.istad.corefeature.facility.repository.FacilityRepository;
import co.istad.corefeature.inventory.facility_inventory.entity.FacilityInventoryEntity;
import co.istad.corefeature.inventory.facility_inventory.repository.FacilityInventoryRepository;
import co.istad.corefeature.order.dto.OrderRequest;
import co.istad.corefeature.order.dto.OrderResponse;
import co.istad.corefeature.order.dto.OrderStatusUpdateRequest;
import co.istad.corefeature.order.entity.OrderEntity;
import co.istad.corefeature.order.mapper.OrderMapper;
import co.istad.corefeature.order.mapper.OrderMapperImpl;
import co.istad.corefeature.order.order_item.dto.OrderItemRequest;
import co.istad.corefeature.order.order_item.entity.OrderItemEntity;
import co.istad.corefeature.order.repository.OrderRepository;
import co.istad.corefeature.product.entity.ProductEntity;
import co.istad.corefeature.product.product_variant.entity.ProductVariantEntity;
import co.istad.corefeature.product.product_variant.repository.ProductVariantRepository;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductVariantRepository productVariantRepository;

    @Mock
    private FacilityRepository facilityRepository;

    @Mock
    private FacilityInventoryRepository facilityInventoryRepository;

    @Mock
    private CartService cartService;

    private OrderMapper orderMapper;
    private OrderService orderService;

    @BeforeEach
    void setUp() {
        orderMapper = new OrderMapperImpl();
        orderService = new OrderServiceImpl(
                orderRepository,
                productVariantRepository,
                facilityRepository,
                facilityInventoryRepository,
                cartService,
                orderMapper
        );
    }

    @Test
    @DisplayName("Should successfully place an online order with 10 line items and real data")
    void createOrder_tenItems_success() {
        Long facilityId = 1L;
        FacilityEntity facility = FacilityEntity.builder()
                .id(facilityId)
                .code("BKK1-FLAGSHIP")
                .name("Phnom Penh Flagship Atelier")
                .type(FacilityType.BRANCH)
                .isActive(true)
                .build();

        when(facilityRepository.findById(facilityId)).thenReturn(Optional.of(facility));

        List<OrderItemRequest> itemRequests = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            Long variantId = (long) i;
            itemRequests.add(new OrderItemRequest(variantId, 1));

            ProductEntity product = ProductEntity.builder()
                    .id((long) i)
                    .name("Luxury Collection Item " + i)
                    .skuCode("LX-SKU-" + String.format("%03d", i))
                    .isActive(true)
                    .build();

            ProductVariantEntity variant = ProductVariantEntity.builder()
                    .id(variantId)
                    .skuId(UUID.randomUUID())
                    .product(product)
                    .price(BigDecimal.valueOf(100.0 * i))
                    .isActive(true)
                    .build();

            FacilityInventoryEntity inventory = FacilityInventoryEntity.builder()
                    .id(variantId)
                    .facility(facility)
                    .productVariant(variant)
                    .quantityOnHand(50)
                    .reservedQuantity(0)
                    .isActive(true)
                    .build();

            when(productVariantRepository.findById(variantId)).thenReturn(Optional.of(variant));
            when(facilityInventoryRepository.findByFacilityIdAndProductVariantId(facilityId, variantId))
                    .thenReturn(Optional.of(inventory));
        }

        AddressRequest address = AddressRequest.builder()
                .addressLine1("No. 128, Preah Norodom Blvd")
                .city("Phnom Penh")
                .province("Phnom Penh")
                .postalCode("12000")
                .country("Cambodia")
                .build();

        UUID cartId = UUID.randomUUID();
        OrderRequest request = OrderRequest.builder()
                .customerId("cust-vip-001")
                .customerName("Menglong Keo")
                .customerEmail("menglong@luxe-atelier.com")
                .customerPhone("+855 12 345 678")
                .facilityId(facilityId)
                .orderType(OrderType.ONLINE)
                .paymentMethod(PaymentMethod.BAKONG_KHQR)
                .shippingAddress(address)
                .cartId(cartId)
                .shippingFee(BigDecimal.valueOf(25))
                .discountAmount(BigDecimal.valueOf(50))
                .items(itemRequests)
                .build();

        when(orderRepository.save(any(OrderEntity.class))).thenAnswer(invocation -> {
            OrderEntity entity = invocation.getArgument(0);
            entity.setId(UUID.randomUUID());
            return entity;
        });

        OrderResponse response = orderService.create(request);

        assertNotNull(response);
        assertNotNull(response.id());
        assertNotNull(response.orderNumber());
        assertEquals("cust-vip-001", response.customerId());
        assertEquals(OrderType.ONLINE, response.orderType());
        assertEquals(OrderStatus.PENDING, response.status());
        assertEquals(10, response.items().size());
        assertEquals(new BigDecimal("5500.0"), response.subtotal());
        assertEquals(new BigDecimal("5475.0"), response.grandTotal());
        verify(facilityInventoryRepository, times(10)).save(any(FacilityInventoryEntity.class));
        verify(cartService).clearItems(cartId);
        verify(orderRepository).save(any(OrderEntity.class));
    }

    @Test
    @DisplayName("Should successfully insert and paginate 10 distinct orders")
    void findAll_tenOrders_success() {
        List<OrderEntity> orders = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            orders.add(OrderEntity.builder()
                    .id(UUID.randomUUID())
                    .orderNumber("LX-20261116-" + String.format("%03d", i))
                    .customerId("cust-" + i)
                    .customerName("VIP Client " + i)
                    .customerEmail("client" + i + "@luxe.com")
                    .orderType(OrderType.ONLINE)
                    .status(OrderStatus.PENDING)
                    .paymentMethod(PaymentMethod.CREDIT_CARD)
                    .paymentStatus(PaymentStatus.PAID)
                    .subtotal(BigDecimal.valueOf(500 * i))
                    .grandTotal(BigDecimal.valueOf(500 * i))
                    .items(new ArrayList<>())
                    .isActive(true)
                    .build());
        }

        Pageable pageable = PageRequest.of(0, 10);
        Page<OrderEntity> orderPage = new PageImpl<>(orders, pageable, 10);

        when(orderRepository.findByIsActiveTrue(pageable)).thenReturn(orderPage);

        Page<OrderResponse> result = orderService.findAll(pageable);

        assertNotNull(result);
        assertEquals(10, result.getTotalElements());
        assertEquals(10, result.getContent().size());
        assertEquals("LX-20261116-001", result.getContent().get(0).orderNumber());
        assertEquals("LX-20261116-010", result.getContent().get(9).orderNumber());
    }

    @Test
    @DisplayName("Should retrieve order by UUID successfully")
    void findById_success() {
        UUID orderId = UUID.randomUUID();
        OrderEntity order = OrderEntity.builder()
                .id(orderId)
                .orderNumber("LX-98241")
                .customerId("cust-101")
                .customerName("Lady Genevieve")
                .customerEmail("genevieve@atelier.com")
                .orderType(OrderType.ONLINE)
                .status(OrderStatus.COMPLETED)
                .paymentMethod(PaymentMethod.BAKONG_KHQR)
                .paymentStatus(PaymentStatus.PAID)
                .subtotal(BigDecimal.valueOf(1930))
                .grandTotal(BigDecimal.valueOf(1930))
                .items(new ArrayList<>())
                .isActive(true)
                .build();

        when(orderRepository.findByIdAndIsActiveTrue(orderId)).thenReturn(Optional.of(order));

        OrderResponse response = orderService.findById(orderId);

        assertNotNull(response);
        assertEquals(orderId, response.id());
        assertEquals("LX-98241", response.orderNumber());
    }

    @Test
    @DisplayName("Should retrieve order by unique order number")
    void findByOrderNumber_success() {
        String orderNum = "LX-20261116-999";
        OrderEntity order = OrderEntity.builder()
                .id(UUID.randomUUID())
                .orderNumber(orderNum)
                .customerId("cust-99")
                .customerName("Lord Vance")
                .customerEmail("vance@atelier.com")
                .orderType(OrderType.ONLINE)
                .status(OrderStatus.PROCESSING)
                .paymentMethod(PaymentMethod.BANK_TRANSFER)
                .subtotal(BigDecimal.valueOf(800))
                .grandTotal(BigDecimal.valueOf(800))
                .items(new ArrayList<>())
                .isActive(true)
                .build();

        when(orderRepository.findByOrderNumber(orderNum)).thenReturn(Optional.of(order));

        OrderResponse response = orderService.findByOrderNumber(orderNum);

        assertNotNull(response);
        assertEquals(orderNum, response.orderNumber());
    }

    @Test
    @DisplayName("Should throw 400 BAD_REQUEST when online order lacks shipping address")
    void createOrder_missingAddress_throwsBadRequest() {
        OrderRequest request = OrderRequest.builder()
                .customerId("cust-001")
                .customerName("Client")
                .customerEmail("client@luxe.com")
                .orderType(OrderType.ONLINE)
                .paymentMethod(PaymentMethod.CASH)
                .shippingAddress(null)
                .items(List.of(new OrderItemRequest(1L, 1)))
                .build();

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> orderService.create(request));
        assertEquals(400, ex.getStatusCode().value());
        assertTrue(ex.getReason().contains("shipping address"));
    }

    @Test
    @DisplayName("Should throw 400 BAD_REQUEST when stock is insufficient at facility")
    void createOrder_insufficientStock_throwsBadRequest() {
        Long facilityId = 1L;
        Long variantId = 1L;

        FacilityEntity facility = FacilityEntity.builder().id(facilityId).name("Main Hub").isActive(true).build();
        when(facilityRepository.findById(facilityId)).thenReturn(Optional.of(facility));

        ProductVariantEntity variant = ProductVariantEntity.builder()
                .id(variantId)
                .price(BigDecimal.valueOf(500))
                .isActive(true)
                .build();
        when(productVariantRepository.findById(variantId)).thenReturn(Optional.of(variant));

        FacilityInventoryEntity inventory = FacilityInventoryEntity.builder()
                .facility(facility)
                .productVariant(variant)
                .quantityOnHand(2)
                .reservedQuantity(0)
                .build();
        when(facilityInventoryRepository.findByFacilityIdAndProductVariantId(facilityId, variantId))
                .thenReturn(Optional.of(inventory));

        AddressRequest address = AddressRequest.builder()
                .addressLine1("Street 1")
                .city("Phnom Penh")
                .country("Cambodia")
                .build();

        OrderRequest request = OrderRequest.builder()
                .customerId("cust-001")
                .customerName("Client")
                .customerEmail("client@luxe.com")
                .facilityId(facilityId)
                .orderType(OrderType.ONLINE)
                .paymentMethod(PaymentMethod.CASH)
                .shippingAddress(address)
                .items(List.of(new OrderItemRequest(variantId, 10)))
                .build();

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> orderService.create(request));
        assertEquals(400, ex.getStatusCode().value());
        assertTrue(ex.getReason().contains("Insufficient stock"));
    }

    @Test
    @DisplayName("Should restock inventory when an order is cancelled")
    void updateStatus_cancelled_restocksInventory() {
        UUID orderId = UUID.randomUUID();
        Long facilityId = 1L;
        Long variantId = 10L;

        ProductVariantEntity variant = ProductVariantEntity.builder().id(variantId).build();
        OrderItemEntity item = OrderItemEntity.builder()
                .id(1L)
                .productVariant(variant)
                .quantity(3)
                .build();

        OrderEntity order = OrderEntity.builder()
                .id(orderId)
                .facilityId(facilityId)
                .status(OrderStatus.PENDING)
                .items(new ArrayList<>(List.of(item)))
                .isActive(true)
                .build();

        FacilityInventoryEntity inventory = FacilityInventoryEntity.builder()
                .quantityOnHand(10)
                .build();

        when(orderRepository.findByIdAndIsActiveTrue(orderId)).thenReturn(Optional.of(order));
        when(facilityInventoryRepository.findByFacilityIdAndProductVariantId(facilityId, variantId))
                .thenReturn(Optional.of(inventory));
        when(orderRepository.save(any(OrderEntity.class))).thenAnswer(i -> i.getArgument(0));

        OrderStatusUpdateRequest updateRequest = OrderStatusUpdateRequest.builder()
                .status(OrderStatus.CANCELLED)
                .rejectionReason("Customer request")
                .build();

        OrderResponse response = orderService.updateStatus(orderId, updateRequest);

        assertNotNull(response);
        assertEquals(OrderStatus.CANCELLED, response.status());
        assertEquals(13, inventory.getQuantityOnHand());
        verify(facilityInventoryRepository).save(inventory);
    }

    @Test
    @DisplayName("Should soft delete an active order")
    void softDeleteById_success() {
        UUID orderId = UUID.randomUUID();
        OrderEntity order = OrderEntity.builder()
                .id(orderId)
                .status(OrderStatus.COMPLETED)
                .items(new ArrayList<>())
                .isActive(true)
                .build();

        when(orderRepository.findByIdAndIsActiveTrue(orderId)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(OrderEntity.class))).thenAnswer(i -> i.getArgument(0));

        OrderResponse response = orderService.softDeleteById(orderId);

        assertNotNull(response);
        assertFalse(order.getIsActive());
        verify(orderRepository).save(order);
    }
}
