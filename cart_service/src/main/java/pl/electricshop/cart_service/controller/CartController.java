package pl.electricshop.cart_service.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.electricshop.cart_service.dto.AddToCartRequest;
import pl.electricshop.cart_service.dto.CartResponse;
import pl.electricshop.cart_service.dto.CheckoutRequest;
import pl.electricshop.cart_service.dto.UpdateQuantityRequest;
import pl.electricshop.cart_service.mapper.CartMapper;
import pl.electricshop.cart_service.model.Cart;
import pl.electricshop.cart_service.service.CartService;

import java.util.UUID;

/**
 * REST Controller for Cart operations.
 * User ID is passed via X-User-ID header (set by API Gateway after JWT validation).
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;
    private final CartMapper cartMapper;

    /**
     * Get current user's cart.
     */
    @GetMapping
    public ResponseEntity<CartResponse> getCart(
            @RequestHeader("X-User-ID") UUID userId) {
        log.info("GET /api/v1/cart for user: {}", userId);

        Cart cart = cartService.getCart(userId);
        return ResponseEntity.ok(cartMapper.toResponse(cart));
    }

    /**
     * Add product to cart.
     */
    @PostMapping("/items")
    public ResponseEntity<CartResponse> addToCart(
            @RequestHeader("X-User-ID") UUID userId,
            @Valid @RequestBody AddToCartRequest request) {
        log.info("POST /api/v1/cart/items for user: {}, product: {}",
                userId, request.getProductNumber());

        Cart cart = cartService.addToCart(
                userId,
                request.getProductNumber(),
                request.getQuantity()
        );
        return ResponseEntity.ok(cartMapper.toResponse(cart));
    }

    /**
     * Update item quantity in cart.
     */
    @PutMapping("/items")
    public ResponseEntity<CartResponse> updateQuantity(
            @RequestHeader("X-User-ID") UUID userId,
            @Valid @RequestBody UpdateQuantityRequest request) {
        log.info("PUT /api/v1/cart/items for user: {}, product: {}, qty: {}",
                userId, request.getProductNumber(), request.getQuantity());

        Cart cart = cartService.updateQuantity(
                userId,
                request.getProductNumber(),
                request.getQuantity()
        );
        return ResponseEntity.ok(cartMapper.toResponse(cart));
    }

    /**
     * Remove product from cart.
     */
    @DeleteMapping("/items/{productNumber}")
    public ResponseEntity<CartResponse> removeFromCart(
            @RequestHeader("X-User-ID") UUID userId,
            @PathVariable String productNumber) {
        log.info("DELETE /api/v1/cart/items/{} for user: {}", productNumber, userId);

        Cart cart = cartService.removeProductFromCart(userId, productNumber);
        return ResponseEntity.ok(cartMapper.toResponse(cart));
    }

    /**
     * Clear entire cart.
     */
    @DeleteMapping
    public ResponseEntity<Void> clearCart(
            @RequestHeader("X-User-ID") UUID userId) {
        log.info("DELETE /api/v1/cart for user: {}", userId);

        cartService.clearCart(userId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Checkout - składa zamówienie.
     * Email jest pobierany z headera X-User-Email (przekazywany przez API Gateway z JWT).
     * AddressId użytkownik wybiera z listy swoich adresów.
     */
    @PostMapping("/checkout")
    public ResponseEntity<Void> checkout(
            @RequestHeader("X-User-ID") UUID userId,
            @RequestHeader("X-User-Email") String email,
            @Valid @RequestBody CheckoutRequest request) {

        // Wywołanie logiki biznesowej (rzucenie eventu na Kafkę)
        cartService.checkout(userId, request.getAddressId(), email);

        // Zwracamy 202 ACCEPTED, bo proces dzieje się w tle (asynchronicznie)
        return ResponseEntity.accepted().build();
    }

}
