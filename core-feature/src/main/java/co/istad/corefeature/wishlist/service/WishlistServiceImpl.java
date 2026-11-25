package co.istad.corefeature.wishlist.service;

import co.istad.corefeature.cart.dto.CartItemRequest;
import co.istad.corefeature.cart.dto.CartRequest;
import co.istad.corefeature.cart.dto.CartResponse;
import co.istad.corefeature.cart.service.CartService;
import co.istad.corefeature.product.product_variant.entity.ProductVariantEntity;
import co.istad.corefeature.product.product_variant.repository.ProductVariantRepository;
import co.istad.corefeature.wishlist.dto.WishlistItemRequest;
import co.istad.corefeature.wishlist.dto.WishlistRequest;
import co.istad.corefeature.wishlist.dto.WishlistResponse;
import co.istad.corefeature.wishlist.entity.WishlistEntity;
import co.istad.corefeature.wishlist.entity.WishlistItemEntity;
import co.istad.corefeature.wishlist.mapper.WishlistMapper;
import co.istad.corefeature.wishlist.repository.WishlistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WishlistServiceImpl implements WishlistService {

    private final WishlistRepository wishlistRepository;
    private final ProductVariantRepository productVariantRepository;
    private final CartService cartService;
    private final WishlistMapper wishlistMapper;

    @Override
    @Transactional
    public WishlistResponse create(WishlistRequest request) {
        if (wishlistRepository.existsByCustomerIdAndIsActiveTrue(request.customerId().trim())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Active wishlist already exists for customer: " + request.customerId());
        }

        WishlistEntity wishlist = wishlistMapper.toEntity(request);
        wishlist.setCustomerId(request.customerId().trim());
        wishlist.setIsActive(true);

        WishlistEntity saved = wishlistRepository.save(wishlist);
        return wishlistMapper.toResponse(saved);
    }

    @Override
    public Page<WishlistResponse> findAll(Pageable pageable) {
        return wishlistRepository.findByIsActiveTrue(pageable)
                .map(wishlistMapper::toResponse);
    }

    @Override
    public WishlistResponse findById(UUID id) {
        WishlistEntity wishlist = wishlistRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Wishlist not found with ID: " + id));
        return wishlistMapper.toResponse(wishlist);
    }

    @Override
    public WishlistResponse findByCustomerId(String customerId) {
        WishlistEntity wishlist = wishlistRepository.findByCustomerIdAndIsActiveTrue(customerId.trim())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Wishlist not found for customer: " + customerId));
        return wishlistMapper.toResponse(wishlist);
    }

    @Override
    @Transactional
    public WishlistResponse addItem(UUID wishlistId, WishlistItemRequest request) {
        WishlistEntity wishlist = wishlistRepository.findByIdAndIsActiveTrue(wishlistId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Wishlist not found with ID: " + wishlistId));

        return appendItem(wishlist, request);
    }

    @Override
    @Transactional
    public WishlistResponse addItemByCustomerId(String customerId, WishlistItemRequest request) {
        WishlistEntity wishlist = getOrCreateWishlist(customerId.trim());
        return appendItem(wishlist, request);
    }

    @Override
    @Transactional
    public WishlistResponse removeItem(UUID wishlistId, Long itemId) {
        WishlistEntity wishlist = wishlistRepository.findByIdAndIsActiveTrue(wishlistId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Wishlist not found with ID: " + wishlistId));

        if (wishlist.getItems() == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Wishlist item not found: " + itemId);
        }

        boolean removed = wishlist.getItems().removeIf(item -> item.getId().equals(itemId));
        if (!removed) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Wishlist item not found with ID: " + itemId);
        }

        WishlistEntity saved = wishlistRepository.save(wishlist);
        return wishlistMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public WishlistResponse removeItemByVariantId(String customerId, Long variantId) {
        WishlistEntity wishlist = wishlistRepository.findByCustomerIdAndIsActiveTrue(customerId.trim())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Wishlist not found for customer: " + customerId));

        if (wishlist.getItems() == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product variant " + variantId + " not found in wishlist");
        }

        boolean removed = wishlist.getItems().removeIf(item ->
                item.getProductVariant() != null && item.getProductVariant().getId().equals(variantId));

        if (!removed) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product variant " + variantId + " not found in wishlist");
        }

        WishlistEntity saved = wishlistRepository.save(wishlist);
        return wishlistMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public WishlistResponse clearItems(UUID wishlistId) {
        WishlistEntity wishlist = wishlistRepository.findByIdAndIsActiveTrue(wishlistId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Wishlist not found with ID: " + wishlistId));

        if (wishlist.getItems() != null) {
            wishlist.getItems().clear();
        }

        WishlistEntity saved = wishlistRepository.save(wishlist);
        return wishlistMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public WishlistResponse clearItemsByCustomerId(String customerId) {
        WishlistEntity wishlist = wishlistRepository.findByCustomerIdAndIsActiveTrue(customerId.trim())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Wishlist not found for customer: " + customerId));

        if (wishlist.getItems() != null) {
            wishlist.getItems().clear();
        }

        WishlistEntity saved = wishlistRepository.save(wishlist);
        return wishlistMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public CartResponse moveToCart(String customerId, Long variantId) {
        WishlistEntity wishlist = wishlistRepository.findByCustomerIdAndIsActiveTrue(customerId.trim())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Wishlist not found for customer: " + customerId));

        if (wishlist.getItems() == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product variant " + variantId + " not in wishlist");
        }

        boolean removed = wishlist.getItems().removeIf(item ->
                item.getProductVariant() != null && item.getProductVariant().getId().equals(variantId));

        if (!removed) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product variant " + variantId + " not found in wishlist");
        }

        wishlistRepository.save(wishlist);

        CartResponse cartResponse;
        try {
            cartResponse = cartService.findByCustomerId(customerId.trim());
        } catch (ResponseStatusException ex) {
            if (ex.getStatusCode() == HttpStatus.NOT_FOUND) {
                cartResponse = cartService.create(new CartRequest(customerId.trim()));
            } else {
                throw ex;
            }
        }

        return cartService.addItem(cartResponse.id(), new CartItemRequest(variantId, 1));
    }

    @Override
    @Transactional
    public WishlistResponse softDeleteById(UUID id) {
        WishlistEntity wishlist = wishlistRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Wishlist not found with ID: " + id));

        wishlist.setIsActive(false);
        WishlistEntity saved = wishlistRepository.save(wishlist);
        return wishlistMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public void permanentDeleteById(UUID id) {
        WishlistEntity wishlist = wishlistRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Wishlist not found with ID: " + id));
        wishlistRepository.delete(wishlist);
    }

    private WishlistResponse appendItem(WishlistEntity wishlist, WishlistItemRequest request) {
        ProductVariantEntity variant = productVariantRepository.findById(request.productVariantId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product variant not found: " + request.productVariantId()));

        if (wishlist.getItems() == null) {
            wishlist.setItems(new ArrayList<>());
        }

        boolean duplicate = wishlist.getItems().stream()
                .anyMatch(item -> item.getProductVariant() != null && item.getProductVariant().getId().equals(request.productVariantId()));

        if (duplicate) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Product variant " + request.productVariantId() + " is already in the wishlist");
        }

        WishlistItemEntity item = WishlistItemEntity.builder()
                .wishlist(wishlist)
                .productVariant(variant)
                .note(request.note())
                .build();

        wishlist.addItem(item);
        WishlistEntity saved = wishlistRepository.save(wishlist);
        return wishlistMapper.toResponse(saved);
    }

    private WishlistEntity getOrCreateWishlist(String customerId) {
        return wishlistRepository.findByCustomerIdAndIsActiveTrue(customerId)
                .orElseGet(() -> {
                    WishlistEntity newWishlist = WishlistEntity.builder()
                            .customerId(customerId)
                            .name("Default Wishlist")
                            .isActive(true)
                            .build();
                    return wishlistRepository.save(newWishlist);
                });
    }
}
