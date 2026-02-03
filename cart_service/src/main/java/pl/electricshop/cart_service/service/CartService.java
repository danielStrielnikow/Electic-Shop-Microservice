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
import pl.electricshop.cart_service.service.gRPC.InventoryGrpcClient;
import pl.electricshop.common.events.cart.CartCheckoutEvent;
import pl.electricshop.common.events.cart.CartItemPayload;
import pl.electricshop.common.events.payment.OrderPlacedEvent;
import pl.electricshop.grpc.AvailabilityResponse;
import pl.electricshop.grpc.ProductCartResponse;
import pl.electricshop.grpc.ReservationResponse;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final CartGrpcService cartGrpcService;
    private final InventoryGrpcClient inventoryGrpcClient;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    private static final int RESERVATION_MINUTES = 15;

    public Cart getCart(UUID userId) {
        return cartRepository.findByUserId(userId)
                .orElse(new Cart(userId, new ArrayList<>()));
    }

    /**
     * Dodaje produkt do koszyka z rezerwacją w Inventory Service.
     */
    public Cart addToCart(UUID userId, String productNumber, int quantity) {
        log.info("Dodawanie produktu {} (qty: {}) do koszyka użytkownika {}", productNumber, quantity, userId);

        // 1. Pobierz dane produktu z Product Service
        ProductCartResponse productData = cartGrpcService.getProductDetails(productNumber);
        if (productData.getProductNumber().isEmpty()) {
            throw new IllegalArgumentException("Produkt " + productNumber + " nie istnieje");
        }

        // 2. Rezerwacja w Inventory Service przez gRPC
        ReservationResponse reservation = inventoryGrpcClient.reserveProduct(
                productNumber,
                quantity,
                userId.toString()
        );

        if (!reservation.getSuccess()) {
            throw new IllegalStateException("Nie udało się zarezerwować produktu: " + reservation.getMessage());
        }

        log.info("Rezerwacja utworzona: {}", reservation.getReservationId());

        // 3. Pobierz lub utwórz koszyk
        Cart cart = getCart(userId);

        // 4. Utwórz CartItem z danymi produktu
        CartItem newItem = new CartItem(
                productData.getProductNumber(),
                productData.getProductName(),
                quantity,
                productData.getDiscount(),
                productData.getPrice()
        );
        newItem.setReservationId(reservation.getReservationId());  // format: "userId:productNumber"

        // 5. Dodaj/aktualizuj w koszyku
        addOrUpdateItem(cart, newItem);

        // 6. Ustaw czas wygaśnięcia rezerwacji
        cart.setReservationUntil(LocalDateTime.now().plusMinutes(RESERVATION_MINUTES));

        return cartRepository.save(cart);
    }

    /**
     * Dodaje nowy item lub aktualizuje ilość istniejącego.
     */
    private void addOrUpdateItem(Cart cart, CartItem newItem) {
        Optional<CartItem> existing = cart.getItems().stream()
                .filter(item -> item.getProductNumber().equals(newItem.getProductNumber()))
                .findFirst();

        if (existing.isPresent()) {
            existing.get().setQuantity(existing.get().getQuantity() + newItem.getQuantity());
            existing.get().setReservationId(newItem.getReservationId());
            log.info("Zaktualizowano ilość produktu {} w koszyku", newItem.getProductNumber());
        } else {
            cart.getItems().add(newItem);
            log.info("Dodano nowy produkt {} do koszyka", newItem.getProductNumber());
        }
    }

    /**
     * Aktualizuje ilość produktu w koszyku wraz z rezerwacją w Inventory Service.
     */
    public Cart updateQuantity(UUID userId, String productNumber, int newQuantity) {
        log.info("Aktualizacja ilości produktu {} na {} dla użytkownika {}", productNumber, newQuantity, userId);

        Cart cart = getCart(userId);

        // Znajdź item w koszyku
        CartItem item = cart.getItems().stream()
                .filter(i -> i.getProductNumber().equals(productNumber))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Produkt " + productNumber + " nie znajduje się w koszyku"));

        if (item.getReservationId() == null) {
            throw new IllegalStateException("Brak rezerwacji dla produktu " + productNumber);
        }

        // Aktualizuj rezerwację w Inventory Service
        var updateResponse = inventoryGrpcClient.updateReservation(item.getReservationId(), newQuantity);

        if (!updateResponse.getSuccess()) {
            throw new IllegalStateException("Nie udało się zaktualizować rezerwacji: " + updateResponse.getMessage());
        }

        // Zaktualizuj ilość w koszyku
        item.setQuantity(newQuantity);

        // Odśwież czas wygaśnięcia rezerwacji
        cart.setReservationUntil(LocalDateTime.now().plusMinutes(RESERVATION_MINUTES));

        log.info("Zaktualizowano ilość produktu {} na {} (rezerwacja: {})", productNumber, newQuantity, item.getReservationId());
        return cartRepository.save(cart);
    }

    /**
     * Usuwa produkt z koszyka i anuluje rezerwację.
     */
    public Cart removeProductFromCart(UUID userId, String productNumber) {
        log.info("Usuwanie produktu {} z koszyka użytkownika {}", productNumber, userId);

        Cart cart = getCart(userId);

        // Znajdź item i anuluj rezerwację
        cart.getItems().stream()
                .filter(item -> item.getProductNumber().equals(productNumber))
                .findFirst()
                .ifPresent(item -> {
                    if (item.getReservationId() != null) {
                        inventoryGrpcClient.cancelReservation(item.getReservationId());
                        log.info("Anulowano rezerwację: {}", item.getReservationId());
                    }
                });

        cart.getItems().removeIf(item -> item.getProductNumber().equals(productNumber));
        return cartRepository.save(cart);
    }

    /**
     * Czyści koszyk i anuluje wszystkie rezerwacje.
     */
    public void clearCart(UUID userId) {
        log.info("Czyszczenie koszyka użytkownika {}", userId);

        Cart cart = getCart(userId);

        // Anuluj wszystkie rezerwacje
        cart.getItems().forEach(item -> {
            if (item.getReservationId() != null) {
                try {
                    inventoryGrpcClient.cancelReservation(item.getReservationId());
                } catch (Exception e) {
                    log.warn("Nie udało się anulować rezerwacji {}: {}", item.getReservationId(), e.getMessage());
                }
            }
        });

        cartRepository.deleteById(userId.toString());
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

        if (cart.getReservationUntil() != null && cart.getReservationUntil().isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("Twoja rezerwacja wygasła. Odśwież koszyk, aby spróbować ponownie.");
        }

        // 3. Mapowanie CartItem -> CartItemPayload
        List<CartItemPayload> eventItems = cart.getItems().stream()
                .map(item -> {
                    BigDecimal price = BigDecimal.valueOf(item.getProductPrice());
                    BigDecimal quantity = BigDecimal.valueOf(item.getQuantity());

                    return new CartItemPayload(
                            item.getProductNumber(),
                            item.getProductName(),
                            item.getQuantity(),
                            price,
                            price.multiply(quantity)
                    );
                })
                .collect(Collectors.toList());

        // 4. Obliczanie całkowitej kwoty
        BigDecimal totalPrice = cart.getTotalPrice();

        // 5. Tworzenie Eventu
        CartCheckoutEvent event = new CartCheckoutEvent(
                userId,
                email,
                addressId,
                totalPrice,
                eventItems
        );

        // 6. Wysyłka na Kafkę
        kafkaTemplate.send("cart-checkout-topic", event);

        log.info("Wysłano event checkoutu na Kafkę dla usera: {}. Kwota: {}", userId, totalPrice);
    }

    /**
     * Po sukcesie płatności - tylko usuwa koszyk z Redis.
     * NIE anuluje rezerwacji - te są obsługiwane przez Inventory Service (handleOrderPlacedEvent).
     */
    @KafkaListener(topics = "order-placed-topic", groupId = "cart-service-group")
    @Transactional
    public void cleanUpCart(OrderPlacedEvent event) {
        if (event.getUserId() == null) {
            log.warn("Otrzymano OrderPlacedEvent bez userId. Nie można wyczyścić koszyka.");
            return;
        }

        log.info("Odebrano OrderPlacedEvent dla usera {}. Usuwanie koszyka (bez anulowania rezerwacji)...", event.getUserId());
        // Tylko usuwamy koszyk z Redis - rezerwacje są już obsługiwane przez Inventory Service
        cartRepository.deleteById(event.getUserId().toString());
    }

}
