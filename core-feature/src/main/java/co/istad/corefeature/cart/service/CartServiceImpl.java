package co.istad.corefeature.cart.service;

import co.istad.corefeature.cart.dto.CartItemRequest;
import co.istad.corefeature.cart.dto.CartItemUpdateRequest;
import co.istad.corefeature.cart.dto.CartRequest;
import co.istad.corefeature.cart.dto.CartResponse;
import co.istad.corefeature.cart.entity.CartEntity;
import co.istad.corefeature.cart.entity.CartItemEntity;
import co.istad.corefeature.cart.mapper.CartMapper;
import co.istad.corefeature.cart.repository.CartRepository;
import co.istad.corefeature.product.product_variant.entity.ProductVariantEntity;
import co.istad.corefeature.product.product_variant.repository.ProductVariantRepository;
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
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final ProductVariantRepository productVariantRepository;
    private final CartMapper cartMapper;

    @Override
    @Transactional
    public CartResponse create(CartRequest request) {
        if (cartRepository.existsByCustomerId(request.customerId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Cart already exists for customer: " + request.customerId());
        }

        CartEntity cart = cartRepository.save(cartMapper.toEntity(request));
        return cartMapper.toResponse(cart);
    }

    @Override
    public Page<CartResponse> findAll(Pageable pageable) {
        Page<CartEntity> cartPage = cartRepository.findByIsActiveTrue(pageable);
        return cartPage.map(cartMapper::toResponse);
    }

    @Override
    public CartResponse findById(UUID id) {
        return cartRepository.findById(id)
                .map(cartMapper::toResponse)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cart not found: " + id));
    }

    @Override
    public CartResponse findByCustomerId(String customerId) {
        return cartRepository.findByCustomerId(customerId)
                .map(cartMapper::toResponse)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cart not found for customer: " + customerId));
    }

    @Override
    @Transactional
    public CartResponse addItem(UUID cartId, CartItemRequest request) {
        CartEntity cart = getCart(cartId);

        ProductVariantEntity productVariant = productVariantRepository.findById(request.productVariantId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product variant not found: " + request.productVariantId()));

        if (cart.getItems() == null) {
            cart.setItems(new ArrayList<>());
        }

        CartItemEntity existing = cart.getItems().stream()
                .filter(item -> item.getProductVariant() != null && item.getProductVariant().getId().equals(request.productVariantId()))
                .findFirst()
                .orElse(null);

        if (existing != null) {
            existing.setQuantity(existing.getQuantity() + request.quantity());
        } else {
            CartItemEntity item = CartItemEntity.builder()
                    .cart(cart)
                    .productVariant(productVariant)
                    .quantity(request.quantity())
                    .build();
            cart.getItems().add(item);
        }

        cart = cartRepository.save(cart);
        return cartMapper.toResponse(cart);
    }

    @Override
    @Transactional
    public CartResponse updateItem(UUID cartId, Long itemId, CartItemUpdateRequest request) {
        CartEntity cart = getCart(cartId);

        if (cart.getItems() == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Cart item not found: " + itemId);
        }

        CartItemEntity item = cart.getItems().stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cart item not found: " + itemId));

        item.setQuantity(request.quantity());

        cart = cartRepository.save(cart);
        return cartMapper.toResponse(cart);
    }

    @Override
    @Transactional
    public CartResponse removeItem(UUID cartId, Long itemId) {
        CartEntity cart = getCart(cartId);

        if (cart.getItems() == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Cart item not found: " + itemId);
        }

        boolean removed = cart.getItems().removeIf(item -> item.getId().equals(itemId));
        if (!removed) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Cart item not found: " + itemId);
        }

        cart = cartRepository.save(cart);
        return cartMapper.toResponse(cart);
    }

    @Override
    @Transactional
    public CartResponse clearItems(UUID cartId) {
        CartEntity cart = getCart(cartId);
        if (cart.getItems() != null) {
            cart.getItems().clear();
        }
        cart = cartRepository.save(cart);
        return cartMapper.toResponse(cart);
    }

    @Override
    @Transactional
    public CartResponse softDeleteById(UUID id) {
        CartEntity cart = getCart(id);
        cart.setIsActive(false);
        return cartMapper.toResponse(cartRepository.save(cart));
    }

    @Override
    @Transactional
    public void permanentDeleteById(UUID id) {
        CartEntity cart = getCart(id);
        cartRepository.delete(cart);
    }

    private CartEntity getCart(UUID id) {
        return cartRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cart not found: " + id));
    }
}
