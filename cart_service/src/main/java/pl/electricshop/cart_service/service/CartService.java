package pl.electricshop.cart_service.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.electricshop.cart_service.model.Cart;
import pl.electricshop.cart_service.model.CartItem;
import pl.electricshop.cart_service.repository.CartRepository;
import pl.electricshop.cart_service.service.gRPC.CartGrpcService;
import pl.electricshop.common.events.cart.CartCheckoutEvent;
import pl.electricshop.common.events.cart.CartItemPayload;
import pl.electricshop.common.events.payment.OrderPlacedEvent;
import pl.electricshop.grpc.ProductCartResponse;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final CartGrpcService cartGrpcService;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public Cart getCart(UUID userId) {
        return cartRepository.findByUserId(userId)
                .orElse(new Cart(userId, new ArrayList<>()));
    }

    /**
     * Dodaje produkt do koszyka, pobierając aktualne dane z Product Service przez gRPC.
     *
     * @param userId        ID użytkownika
     * @param productNumber numer produktu (np. "EL-ABC123")
     * @param quantity      ilość do dodania
     * @return zaktualizowany koszyk
     */
    public Cart addToCart(UUID userId, String productNumber, int quantity) {
        log.info("Adding product {} (qty: {}) to cart for user {}", productNumber, quantity, userId);

        // 1. Pobierz aktualne dane produktu przez gRPC
        ProductCartResponse productData = cartGrpcService.getProductDetails(productNumber);

        // 2. Sprawdź czy produkt istnieje
        if (productData.getProductNumber().isEmpty()) {
            throw new IllegalArgumentException("Produkt o numerze " + productNumber + " nie istnieje");
        }

        // 3. Sprawdź dostępność stock
        if (productData.getQuantity() < quantity) {
            throw new IllegalStateException(
                    "Niewystarczająca ilość produktu. Dostępne: " + productData.getQuantity()
            );
        }

        // 4. Utwórz CartItem ze SNAPSHOT danych produktu
        CartItem newItem = new CartItem(
                productData.getProductNumber(),
                productData.getProductName(),
                quantity,
                (double) productData.getDiscount(),
                productData.getPrice()
        );

        // 5. Dodaj do koszyka
        return addItemToCart(userId, newItem);
    }

    /**
     * Wewnętrzna metoda dodająca CartItem do koszyka.
     */
    private Cart addItemToCart(UUID userId, CartItem newItem) {
        Cart cart = getCart(userId);

        Optional<CartItem> existingItem = cart.getItems().stream()
                .filter(item -> item.getProductNumber().equals(newItem.getProductNumber()))
                .findFirst();

        if (existingItem.isPresent()) {
            // Aktualizuj ilość istniejącego produktu
            existingItem.get().setQuantity(existingItem.get().getQuantity() + newItem.getQuantity());
            log.info("Updated quantity for product {} in cart", newItem.getProductNumber());
        } else {
            // Dodaj nowy produkt
            cart.getItems().add(newItem);
            log.info("Added new product {} to cart", newItem.getProductNumber());
        }

        return cartRepository.save(cart);
    }

    public Cart updateQuantity(UUID userId, String productNumber, int newQuantity) {
        log.info("Updating quantity for product {} to {} for user {}", productNumber, newQuantity, userId);

        // Walidacja przez gRPC
        ProductCartResponse productData = cartGrpcService.getProductDetails(productNumber);
        if (productData.getQuantity() < newQuantity) {
            throw new IllegalStateException(
                    "Niewystarczająca ilość produktu. Dostępne: " + productData.getQuantity()
            );
        }

        Cart cart = getCart(userId);

        cart.getItems().stream()
                .filter(item -> item.getProductNumber().equals(productNumber))
                .findFirst()
                .ifPresent(item -> item.setQuantity(newQuantity));

        return cartRepository.save(cart);
    }

    public Cart removeProductFromCart(UUID userId, String productNumber) {
        log.info("Removing product {} from cart for user {}", productNumber, userId);
        Cart cart = getCart(userId);
        cart.getItems().removeIf(item -> item.getProductNumber().equals(productNumber));
        return cartRepository.save(cart);
    }

    public void clearCart(UUID userId) {
        log.info("Clearing cart for user {}", userId);
        cartRepository.deleteById(userId.toString());
    }

    @KafkaListener(topics = "order-placed-topic", groupId = "cart-service-group")
    @Transactional
    public void cleanUpCart(OrderPlacedEvent event) {
        log.info("Zamówienie nr {} udane. Usuwam koszyk usera {}", event.getEmail(), event.getEmail());

        if (event.getUserId() != null) {
            clearCart(event.getUserId());
        } else {
            log.warn("Nie można usunąć koszyka - brak userId w evencie OrderPlacedEvent");
        }
    }

    /**
     * Główna metoda checkoutu - zbiera dane i wysyła na Kafkę.
     */
    public void checkout(UUID userId, UUID addressId, String email) {
        log.info("Rozpoczynam checkout dla usera: {}", userId);

        // 1. Pobierz koszyk
        Cart cart = getCart(userId);

        // 2. Walidacja: Pusty koszyk nie może być zamówiony
        if (cart.getItems().isEmpty()) {
            throw new IllegalStateException("Koszyk jest pusty! Nie można złożyć zamówienia.");
        }

        // 3. Mapowanie CartItem (Redis/Mongo) -> CartItemPayload (Event/Kafka)
        // Musimy zamienić nasze wewnętrzne obiekty na te wspólne z common-events
        List<CartItemPayload> eventItems = cart.getItems().stream()
                .map(item -> {
                    BigDecimal price = BigDecimal.valueOf(item.getProductPrice());
                    BigDecimal quantity = BigDecimal.valueOf(item.getQuantity());

                    return new CartItemPayload(
                            item.getProductNumber(),
                            item.getProductName(),
                            item.getQuantity(),
                            price,
                            price.multiply(quantity) // Cena całkowita linii
                    );
                })
                .collect(Collectors.toList());

        // 4. Obliczanie całkowitej kwoty (Total Price)
        BigDecimal totalPrice = cart.getTotalPrice();

        // 5. Tworzenie Eventu
        CartCheckoutEvent event = new CartCheckoutEvent(
                userId,
                email,
                addressId, // ID adresu przekazane z Frontendu
                totalPrice,
                eventItems
        );

        // 6. Wysyłka na Kafkę
        // Topic "cart-checkout-topic" musi być taki sam jak w @KafkaListener w OrderService
        kafkaTemplate.send("cart-checkout-topic", event);

        log.info("Wysłano event checkoutu na Kafkę dla usera: {}. Kwota: {}", userId, totalPrice);

        // UWAGA: Nie czyścimy koszyka tutaj!
        // Koszyk czyścimy dopiero jak przyjdzie event "OrderPlacedEvent" (metoda cleanUpCart na dole).
    }
}
