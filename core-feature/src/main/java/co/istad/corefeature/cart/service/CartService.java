package co.istad.corefeature.cart.service;

import co.istad.corefeature.cart.dto.CartItemRequest;
import co.istad.corefeature.cart.dto.CartItemUpdateRequest;
import co.istad.corefeature.cart.dto.CartRequest;
import co.istad.corefeature.cart.dto.CartResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface CartService {

    CartResponse create(CartRequest request);

    Page<CartResponse> findAll(Pageable pageable);

    CartResponse findById(UUID id);

    CartResponse findByCustomerId(String customerId);

    CartResponse addItem(UUID cartId, CartItemRequest request);

    CartResponse updateItem(UUID cartId, Long itemId, CartItemUpdateRequest request);

    CartResponse removeItem(UUID cartId, Long itemId);

    CartResponse clearItems(UUID cartId);

    CartResponse softDeleteById(UUID id);

    void permanentDeleteById(UUID id);
}
