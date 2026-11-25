package co.istad.corefeature.wishlist.service;

import co.istad.corefeature.cart.dto.CartItemRequest;
import co.istad.corefeature.cart.dto.CartItemResponse;
import co.istad.corefeature.cart.dto.CartResponse;
import co.istad.corefeature.cart.service.CartService;
import co.istad.corefeature.product.entity.ProductEntity;
import co.istad.corefeature.product.product_variant.entity.ProductVariantEntity;
import co.istad.corefeature.product.product_variant.repository.ProductVariantRepository;
import co.istad.corefeature.wishlist.dto.WishlistItemRequest;
import co.istad.corefeature.wishlist.dto.WishlistItemResponse;
import co.istad.corefeature.wishlist.dto.WishlistRequest;
import co.istad.corefeature.wishlist.dto.WishlistResponse;
import co.istad.corefeature.wishlist.entity.WishlistEntity;
import co.istad.corefeature.wishlist.entity.WishlistItemEntity;
import co.istad.corefeature.wishlist.mapper.WishlistMapper;
import co.istad.corefeature.wishlist.repository.WishlistRepository;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WishlistServiceImplTest {

    @Mock
    private WishlistRepository wishlistRepository;

    @Mock
    private ProductVariantRepository productVariantRepository;

    @Mock
    private CartService cartService;

    @Mock
    private WishlistMapper wishlistMapper;

    private WishlistService wishlistService;

    private UUID wishlistId;
    private WishlistEntity wishlistEntity;
    private ProductVariantEntity variantEntity;
    private WishlistResponse wishlistResponse;

    @BeforeEach
    void setUp() {
        wishlistService = new WishlistServiceImpl(
                wishlistRepository,
                productVariantRepository,
                cartService,
                wishlistMapper
        );

        wishlistId = UUID.randomUUID();

        ProductEntity product = ProductEntity.builder()
                .name("Vicuna Double-Breasted Blazer")
                .skuCode("LX-PRD-001")
                .build();

        variantEntity = ProductVariantEntity.builder()
                .id(1L)
                .product(product)
                .price(BigDecimal.valueOf(4500.00))
                .isActive(true)
                .build();

        wishlistEntity = WishlistEntity.builder()
                .id(wishlistId)
                .customerId("cust-vip-001")
                .name("Genevieve's Winter Salon Curation")
                .items(new ArrayList<>())
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        WishlistItemResponse itemResponse = WishlistItemResponse.builder()
                .id(101L)
                .productVariantId(1L)
                .productName("Vicuna Double-Breasted Blazer")
                .skuCode("LX-PRD-001")
                .price(BigDecimal.valueOf(4500.00))
                .note("Autumn salon preview")
                .addedAt(LocalDateTime.now())
                .build();

        wishlistResponse = WishlistResponse.builder()
                .id(wishlistId)
                .customerId("cust-vip-001")
                .name("Genevieve's Winter Salon Curation")
                .items(List.of(itemResponse))
                .totalItems(1)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Should successfully create a customer wishlist")
    void createWishlist_success() {
        WishlistRequest request = new WishlistRequest("cust-vip-001", "Genevieve's Winter Salon Curation");

        when(wishlistRepository.existsByCustomerIdAndIsActiveTrue("cust-vip-001")).thenReturn(false);
        when(wishlistMapper.toEntity(request)).thenReturn(wishlistEntity);
        when(wishlistRepository.save(wishlistEntity)).thenReturn(wishlistEntity);
        when(wishlistMapper.toResponse(wishlistEntity)).thenReturn(wishlistResponse);

        WishlistResponse response = wishlistService.create(request);

        assertNotNull(response);
        assertEquals("cust-vip-001", response.customerId());
        assertEquals("Genevieve's Winter Salon Curation", response.name());
        verify(wishlistRepository, times(1)).save(wishlistEntity);
    }

    @Test
    @DisplayName("Should throw 409 CONFLICT if active wishlist already exists for customer")
    void createWishlist_conflict() {
        WishlistRequest request = new WishlistRequest("cust-vip-001", "Genevieve's Winter Salon Curation");

        when(wishlistRepository.existsByCustomerIdAndIsActiveTrue("cust-vip-001")).thenReturn(true);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> wishlistService.create(request));
        assertEquals(409, ex.getStatusCode().value());
        assertTrue(ex.getReason().contains("already exists"));
        verify(wishlistRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should retrieve wishlist by ID")
    void findById_success() {
        when(wishlistRepository.findByIdAndIsActiveTrue(wishlistId)).thenReturn(Optional.of(wishlistEntity));
        when(wishlistMapper.toResponse(wishlistEntity)).thenReturn(wishlistResponse);

        WishlistResponse response = wishlistService.findById(wishlistId);

        assertNotNull(response);
        assertEquals(wishlistId, response.id());
    }

    @Test
    @DisplayName("Should throw 404 NOT_FOUND when wishlist ID does not exist")
    void findById_notFound() {
        when(wishlistRepository.findByIdAndIsActiveTrue(wishlistId)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> wishlistService.findById(wishlistId));
    }

    @Test
    @DisplayName("Should retrieve active wishlist by customer ID")
    void findByCustomerId_success() {
        when(wishlistRepository.findByCustomerIdAndIsActiveTrue("cust-vip-001")).thenReturn(Optional.of(wishlistEntity));
        when(wishlistMapper.toResponse(wishlistEntity)).thenReturn(wishlistResponse);

        WishlistResponse response = wishlistService.findByCustomerId("cust-vip-001");

        assertNotNull(response);
        assertEquals("cust-vip-001", response.customerId());
    }

    @Test
    @DisplayName("Should add product variant to wishlist")
    void addItem_success() {
        WishlistItemRequest request = new WishlistItemRequest(1L, "Autumn salon preview");

        when(wishlistRepository.findByIdAndIsActiveTrue(wishlistId)).thenReturn(Optional.of(wishlistEntity));
        when(productVariantRepository.findById(1L)).thenReturn(Optional.of(variantEntity));
        when(wishlistRepository.save(wishlistEntity)).thenReturn(wishlistEntity);
        when(wishlistMapper.toResponse(wishlistEntity)).thenReturn(wishlistResponse);

        WishlistResponse response = wishlistService.addItem(wishlistId, request);

        assertNotNull(response);
        assertEquals(1, wishlistEntity.getItems().size());
        verify(wishlistRepository, times(1)).save(wishlistEntity);
    }

    @Test
    @DisplayName("Should throw 409 CONFLICT if product variant already exists in wishlist")
    void addItem_duplicateVariant_throwsConflict() {
        WishlistItemEntity existingItem = WishlistItemEntity.builder()
                .wishlist(wishlistEntity)
                .productVariant(variantEntity)
                .build();
        wishlistEntity.getItems().add(existingItem);

        WishlistItemRequest request = new WishlistItemRequest(1L, "Duplicate add");

        when(wishlistRepository.findByIdAndIsActiveTrue(wishlistId)).thenReturn(Optional.of(wishlistEntity));
        when(productVariantRepository.findById(1L)).thenReturn(Optional.of(variantEntity));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> wishlistService.addItem(wishlistId, request));

        assertEquals(409, ex.getStatusCode().value());
        assertTrue(ex.getReason().contains("already in the wishlist"));
    }

    @Test
    @DisplayName("Should remove item by variant ID from customer wishlist")
    void removeItemByVariantId_success() {
        WishlistItemEntity existingItem = WishlistItemEntity.builder()
                .id(101L)
                .wishlist(wishlistEntity)
                .productVariant(variantEntity)
                .build();
        wishlistEntity.getItems().add(existingItem);

        when(wishlistRepository.findByCustomerIdAndIsActiveTrue("cust-vip-001")).thenReturn(Optional.of(wishlistEntity));
        when(wishlistRepository.save(wishlistEntity)).thenReturn(wishlistEntity);
        when(wishlistMapper.toResponse(wishlistEntity)).thenReturn(wishlistResponse);

        WishlistResponse response = wishlistService.removeItemByVariantId("cust-vip-001", 1L);

        assertNotNull(response);
        assertEquals(0, wishlistEntity.getItems().size());
        verify(wishlistRepository, times(1)).save(wishlistEntity);
    }

    @Test
    @DisplayName("Should clear all items from wishlist")
    void clearItems_success() {
        WishlistItemEntity item = WishlistItemEntity.builder()
                .id(101L)
                .wishlist(wishlistEntity)
                .productVariant(variantEntity)
                .build();
        wishlistEntity.getItems().add(item);

        when(wishlistRepository.findByIdAndIsActiveTrue(wishlistId)).thenReturn(Optional.of(wishlistEntity));
        when(wishlistRepository.save(wishlistEntity)).thenReturn(wishlistEntity);
        when(wishlistMapper.toResponse(wishlistEntity)).thenReturn(wishlistResponse);

        WishlistResponse response = wishlistService.clearItems(wishlistId);

        assertNotNull(response);
        assertEquals(0, wishlistEntity.getItems().size());
        verify(wishlistRepository, times(1)).save(wishlistEntity);
    }

    @Test
    @DisplayName("Should move wishlisted item to cart successfully")
    void moveToCart_success() {
        WishlistItemEntity item = WishlistItemEntity.builder()
                .id(101L)
                .wishlist(wishlistEntity)
                .productVariant(variantEntity)
                .build();
        wishlistEntity.getItems().add(item);

        UUID cartId = UUID.randomUUID();
        CartResponse cartResponse = CartResponse.builder()
                .id(cartId)
                .customerId("cust-vip-001")
                .items(List.of(new CartItemResponse(201L, 1L, 1)))
                .isActive(true)
                .build();

        when(wishlistRepository.findByCustomerIdAndIsActiveTrue("cust-vip-001")).thenReturn(Optional.of(wishlistEntity));
        when(cartService.findByCustomerId("cust-vip-001")).thenReturn(cartResponse);
        when(cartService.addItem(eq(cartId), any(CartItemRequest.class))).thenReturn(cartResponse);

        CartResponse result = wishlistService.moveToCart("cust-vip-001", 1L);

        assertNotNull(result);
        assertEquals(cartId, result.id());
        assertEquals(0, wishlistEntity.getItems().size());
        verify(wishlistRepository, times(1)).save(wishlistEntity);
        verify(cartService, times(1)).addItem(eq(cartId), any(CartItemRequest.class));
    }

    @Test
    @DisplayName("Should soft delete wishlist")
    void softDeleteById_success() {
        when(wishlistRepository.findByIdAndIsActiveTrue(wishlistId)).thenReturn(Optional.of(wishlistEntity));
        when(wishlistRepository.save(wishlistEntity)).thenReturn(wishlistEntity);
        when(wishlistMapper.toResponse(wishlistEntity)).thenReturn(wishlistResponse);

        WishlistResponse response = wishlistService.softDeleteById(wishlistId);

        assertNotNull(response);
        assertFalse(wishlistEntity.getIsActive());
        verify(wishlistRepository, times(1)).save(wishlistEntity);
    }

    @Test
    @DisplayName("Should hard delete wishlist permanently")
    void permanentDeleteById_success() {
        when(wishlistRepository.findById(wishlistId)).thenReturn(Optional.of(wishlistEntity));

        wishlistService.permanentDeleteById(wishlistId);

        verify(wishlistRepository, times(1)).delete(wishlistEntity);
    }
}
