package co.istad.corefeature.wishlist.service;

import co.istad.corefeature.cart.dto.CartResponse;
import co.istad.corefeature.wishlist.dto.WishlistItemRequest;
import co.istad.corefeature.wishlist.dto.WishlistRequest;
import co.istad.corefeature.wishlist.dto.WishlistResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface WishlistService {

    WishlistResponse create(WishlistRequest request);

    Page<WishlistResponse> findAll(Pageable pageable);

    WishlistResponse findById(UUID id);

    WishlistResponse findByCustomerId(String customerId);

    WishlistResponse addItem(UUID wishlistId, WishlistItemRequest request);

    WishlistResponse addItemByCustomerId(String customerId, WishlistItemRequest request);

    WishlistResponse removeItem(UUID wishlistId, Long itemId);

    WishlistResponse removeItemByVariantId(String customerId, Long variantId);

    WishlistResponse clearItems(UUID wishlistId);

    WishlistResponse clearItemsByCustomerId(String customerId);

    CartResponse moveToCart(String customerId, Long variantId);

    WishlistResponse softDeleteById(UUID id);

    void permanentDeleteById(UUID id);
}
