package co.istad.corefeature.order.service;

import co.istad.corefeature.cart.service.CartService;
import co.istad.corefeature.common.enums.OrderStatus;
import co.istad.corefeature.common.enums.OrderType;
import co.istad.corefeature.common.enums.PaymentStatus;
import co.istad.corefeature.facility.entity.FacilityEntity;
import co.istad.corefeature.facility.repository.FacilityRepository;
import co.istad.corefeature.inventory.facility_inventory.entity.FacilityInventoryEntity;
import co.istad.corefeature.inventory.facility_inventory.repository.FacilityInventoryRepository;
import co.istad.corefeature.order.dto.OrderRequest;
import co.istad.corefeature.order.dto.OrderResponse;
import co.istad.corefeature.order.dto.OrderStatusUpdateRequest;
import co.istad.corefeature.order.entity.OrderEntity;
import co.istad.corefeature.order.mapper.OrderMapper;
import co.istad.corefeature.order.order_item.dto.OrderItemRequest;
import co.istad.corefeature.order.order_item.entity.OrderItemEntity;
import co.istad.corefeature.order.repository.OrderRepository;
import co.istad.corefeature.product.product_variant.entity.ProductVariantEntity;
import co.istad.corefeature.product.product_variant.repository.ProductVariantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final ProductVariantRepository productVariantRepository;
    private final FacilityRepository facilityRepository;
    private final FacilityInventoryRepository facilityInventoryRepository;
    private final CartService cartService;
    private final OrderMapper orderMapper;
    private static final SecureRandom RANDOM = new SecureRandom();

    @Override
    @Transactional
    public OrderResponse create(OrderRequest request) {
        if (request.orderType() == OrderType.ONLINE) {
            if (request.shippingAddress() == null
                    || request.shippingAddress().addressLine1() == null || request.shippingAddress().addressLine1().isBlank()
                    || request.shippingAddress().city() == null || request.shippingAddress().city().isBlank()
                    || request.shippingAddress().country() == null || request.shippingAddress().country().isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Complete shipping address is required for online orders");
            }
        }

        final FacilityEntity facility = request.facilityId() != null
                ? facilityRepository.findById(request.facilityId())
                .filter(FacilityEntity::getIsActive)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Facility not found or inactive: " + request.facilityId()))
                : null;

        if (request.items() == null || request.items().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Order must contain at least one item");
        }

        List<OrderItemEntity> orderItems = new ArrayList<>();
        BigDecimal subtotal = BigDecimal.ZERO;

        for (OrderItemRequest itemReq : request.items()) {
            ProductVariantEntity variant = productVariantRepository.findById(itemReq.productVariantId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product variant not found: " + itemReq.productVariantId()));

            if (!Boolean.TRUE.equals(variant.getIsActive())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Product variant is currently inactive: " + itemReq.productVariantId());
            }

            if (facility != null) {
                FacilityInventoryEntity inventory = facilityInventoryRepository
                        .findByFacilityIdAndProductVariantId(facility.getId(), variant.getId())
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Inventory record not found for variant ID: " + variant.getId() + " at facility: " + facility.getName()));

                int availableStock = inventory.getQuantityOnHand() - inventory.getReservedQuantity();
                if (availableStock < itemReq.quantity()) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Insufficient stock for product variant ID " + variant.getId() + ". Available: " + availableStock + ", Requested: " + itemReq.quantity());
                }

                inventory.setQuantityOnHand(inventory.getQuantityOnHand() - itemReq.quantity());
                facilityInventoryRepository.save(inventory);
            }

            BigDecimal unitPrice = variant.getPrice();
            BigDecimal lineTotal = unitPrice.multiply(BigDecimal.valueOf(itemReq.quantity()));
            subtotal = subtotal.add(lineTotal);

            String productName = variant.getProduct() != null ? variant.getProduct().getName() : "Unknown Product";
            String skuCode = variant.getProduct() != null ? variant.getProduct().getSkuCode() : null;

            OrderItemEntity orderItem = OrderItemEntity.builder()
                    .productVariant(variant)
                    .productName(productName)
                    .skuCode(skuCode)
                    .quantity(itemReq.quantity())
                    .unitPrice(unitPrice)
                    .totalPrice(lineTotal)
                    .build();

            orderItems.add(orderItem);
        }

        BigDecimal shippingFee = request.shippingFee() != null ? request.shippingFee() : BigDecimal.ZERO;
        BigDecimal discountAmount = request.discountAmount() != null ? request.discountAmount() : BigDecimal.ZERO;
        BigDecimal grandTotal = subtotal.add(shippingFee).subtract(discountAmount);
        if (grandTotal.compareTo(BigDecimal.ZERO) < 0) {
            grandTotal = BigDecimal.ZERO;
        }

        String orderNumber = generateUniqueOrderNumber();

        OrderEntity order = OrderEntity.builder()
                .orderNumber(orderNumber)
                .customerId(request.customerId())
                .customerName(request.customerName())
                .customerEmail(request.customerEmail())
                .customerPhone(request.customerPhone())
                .facilityId(facility != null ? facility.getId() : null)
                .orderType(request.orderType())
                .status(OrderStatus.PENDING)
                .subtotal(subtotal)
                .shippingFee(shippingFee)
                .discountAmount(discountAmount)
                .grandTotal(grandTotal)
                .paymentMethod(request.paymentMethod())
                .paymentStatus(PaymentStatus.UNPAID)
                .shippingAddress(request.shippingAddress() != null ? orderMapper.toAddress(request.shippingAddress()) : null)
                .items(new ArrayList<>())
                .isActive(true)
                .build();

        for (OrderItemEntity item : orderItems) {
            order.addItem(item);
        }

        OrderEntity savedOrder = orderRepository.save(order);

        if (request.cartId() != null) {
            try {
                cartService.clearItems(request.cartId());
            } catch (Exception ignored) {
            }
        }

        return orderMapper.toResponse(savedOrder);
    }

    @Override
    public Page<OrderResponse> findAll(Pageable pageable) {
        return orderRepository.findByIsActiveTrue(pageable).map(orderMapper::toResponse);
    }

    @Override
    public OrderResponse findById(UUID id) {
        return orderRepository.findByIdAndIsActiveTrue(id)
                .map(orderMapper::toResponse)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found: " + id));
    }

    @Override
    public OrderResponse findByOrderNumber(String orderNumber) {
        return orderRepository.findByOrderNumber(orderNumber)
                .filter(OrderEntity::getIsActive)
                .map(orderMapper::toResponse)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found with order number: " + orderNumber));
    }

    @Override
    public Page<OrderResponse> findByCustomerId(String customerId, Pageable pageable) {
        return orderRepository.findByCustomerIdAndIsActiveTrue(customerId, pageable).map(orderMapper::toResponse);
    }

    @Override
    @Transactional
    public OrderResponse updateStatus(UUID id, OrderStatusUpdateRequest request) {
        OrderEntity order = orderRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found: " + id));

        if (order.getStatus() == OrderStatus.COMPLETED && request.status() != OrderStatus.COMPLETED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Completed order status cannot be modified");
        }

        if ((order.getStatus() == OrderStatus.CANCELLED || order.getStatus() == OrderStatus.REJECTED)
                && request.status() != order.getStatus()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Terminated order status cannot be reopened");
        }

        if ((request.status() == OrderStatus.CANCELLED || request.status() == OrderStatus.REJECTED)
                && order.getStatus() != OrderStatus.CANCELLED
                && order.getStatus() != OrderStatus.REJECTED) {
            if (order.getFacilityId() != null) {
                for (OrderItemEntity item : order.getItems()) {
                    facilityInventoryRepository.findByFacilityIdAndProductVariantId(order.getFacilityId(), item.getProductVariant().getId())
                            .ifPresent(inventory -> {
                                inventory.setQuantityOnHand(inventory.getQuantityOnHand() + item.getQuantity());
                                facilityInventoryRepository.save(inventory);
                            });
                }
            }
            if (request.rejectionReason() != null) {
                order.setRejectionReason(request.rejectionReason());
            }
        }

        order.setStatus(request.status());

        if (request.paymentStatus() != null) {
            order.setPaymentStatus(request.paymentStatus());
        } else if (request.status() == OrderStatus.COMPLETED && order.getPaymentStatus() == PaymentStatus.UNPAID) {
            order.setPaymentStatus(PaymentStatus.PAID);
        }

        return orderMapper.toResponse(orderRepository.save(order));
    }

    @Override
    @Transactional
    public OrderResponse softDeleteById(UUID id) {
        OrderEntity order = orderRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found: " + id));

        if (order.getStatus() == OrderStatus.PENDING || order.getStatus() == OrderStatus.PROCESSING) {
            if (order.getFacilityId() != null) {
                for (OrderItemEntity item : order.getItems()) {
                    facilityInventoryRepository.findByFacilityIdAndProductVariantId(order.getFacilityId(), item.getProductVariant().getId())
                            .ifPresent(inventory -> {
                                inventory.setQuantityOnHand(inventory.getQuantityOnHand() + item.getQuantity());
                                facilityInventoryRepository.save(inventory);
                            });
                }
            }
        }

        order.setIsActive(false);
        return orderMapper.toResponse(orderRepository.save(order));
    }

    @Override
    @Transactional
    public void permanentDeleteById(UUID id) {
        OrderEntity order = orderRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found: " + id));
        orderRepository.delete(order);
    }

    private String generateUniqueOrderNumber() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        int randomSuffix = 100 + RANDOM.nextInt(900);
        return "LX-" + timestamp + "-" + randomSuffix;
    }
}
