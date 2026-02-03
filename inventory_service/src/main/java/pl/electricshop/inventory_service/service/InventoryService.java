package pl.electricshop.inventory_service.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import pl.electricshop.common.events.product.ProductEvent;
import pl.electricshop.inventory_service.model.Inventory;
import pl.electricshop.inventory_service.model.api.ProductReservationDTO;
import pl.electricshop.inventory_service.repository.InventoryRepository;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final RedisTemplate<String, String> redisTemplate;

    private static final Duration RESERVATION_TTL = Duration.ofMinutes(15);

    /**
     * Tworzy tymczasową rezerwację produktu.
     * Klucz Redis: "reservation:{userId}:{productNumber}" -> quantity
     *
     * @return reservationId (format: "{userId}:{productNumber}") lub null jeśli brak dostępności
     */
    @Transactional
    public String createTemporaryReservation(String userId, String productNumber, int quantity) {
        Inventory inventory = inventoryRepository.findByProductNumber(productNumber)
                .orElseThrow(() -> new RuntimeException("Produkt nie istnieje w magazynie: " + productNumber));

        // Sprawdź czy jest wystarczająca ilość
        if (inventory.getAvailableQuantity() < quantity) {
            log.warn("Niewystarczająca ilość produktu {}. Dostępne: {}, żądane: {}",
                    productNumber, inventory.getAvailableQuantity(), quantity);
            return null;
        }

        // Zaktualizuj stan magazynowy
        inventory.setAvailableQuantity(inventory.getAvailableQuantity() - quantity);
        inventory.setReservedQuantity(inventory.getReservedQuantity() + quantity);
        inventoryRepository.save(inventory);

        // Zapisz rezerwację w Redis z TTL
        String reservationId = userId + ":" + productNumber;
        String redisKey = "reservation:" + reservationId;
        redisTemplate.opsForValue().set(redisKey, String.valueOf(quantity), RESERVATION_TTL);

        log.info("Utworzono rezerwację: {} dla {} szt. produktu {}", reservationId, quantity, productNumber);
        return reservationId;
    }

    /**
     * Sprawdza dostępną ilość produktu.
     */
    public int getAvailableQuantity(String productNumber) {
        return inventoryRepository.findByProductNumber(productNumber)
                .map(Inventory::getAvailableQuantity)
                .orElse(0);
    }

    /**
     * Sprawdza czy produkt jest dostępny w podanej ilości.
     */
    public boolean checkProductAvailability(String productNumber, int requiredQuantity) {
        Inventory inventory = inventoryRepository.findByProductNumber(productNumber)
                .orElse(null);

        if (inventory == null) {
            log.warn("Produkt {} nie istnieje w magazynie", productNumber);
            return false;
        }

        return inventory.getAvailableQuantity() >= requiredQuantity;
    }

    /**
     * Anuluje rezerwację i przywraca dostępność produktu.
     *
     * @param reservationId format: "{userId}:{productNumber}"
     */
    @Transactional
    public ProductReservationDTO cancelReservation(String reservationId) {
        String redisKey = "reservation:" + reservationId;
        String quantityStr = redisTemplate.opsForValue().get(redisKey);

        if (quantityStr == null) {
            log.warn("Brak rezerwacji do anulowania: {}", reservationId);
            throw new RuntimeException("Rezerwacja nie istnieje lub wygasła: " + reservationId);
        }

        int quantity = Integer.parseInt(quantityStr);

        // Wyciągnij productNumber z reservationId (format: "userId:productNumber")
        String[] parts = reservationId.split(":");
        if (parts.length < 2) {
            throw new RuntimeException("Nieprawidłowy format reservationId: " + reservationId);
        }
        String userId = parts[0];
        String productNumber = parts[1];

        // Przywróć dostępność w magazynie
        Inventory inventory = inventoryRepository.findByProductNumber(productNumber)
                .orElseThrow(() -> new RuntimeException("Produkt nie istnieje: " + productNumber));

        inventory.setAvailableQuantity(inventory.getAvailableQuantity() + quantity);
        inventory.setReservedQuantity(Math.max(0, inventory.getReservedQuantity() - quantity));
        inventoryRepository.save(inventory);

        // Usuń rezerwację z Redis
        redisTemplate.delete(redisKey);

        log.info("Anulowano rezerwację: {} - przywrócono {} szt. produktu {}", reservationId, quantity, productNumber);
        return new ProductReservationDTO(productNumber, quantity, UUID.fromString(userId));
    }

    /**
     * Kafka listener - obsługa dodania nowego produktu.
     */
    @KafkaListener(topics = "product-add-topic", groupId = "inventory-service-group")
    @Transactional
    public void handleProductAddEvent(ProductEvent event) {
        log.info("Otrzymano event dodania produktu: {}", event);

        String productNumber = event.productNumber();
        int quantity = event.quantity();

        // Sprawdź czy produkt już istnieje
        Optional<Inventory> existing = inventoryRepository.findByProductNumber(productNumber);
        if (existing.isPresent()) {
            log.warn("Produkt {} już istnieje w magazynie - aktualizuję ilość", productNumber);
            Inventory inventory = existing.get();
            inventory.setAvailableQuantity(inventory.getAvailableQuantity() + quantity);
            inventoryRepository.save(inventory);
            return;
        }

        // Utwórz nowy rekord inventory
        Inventory inventory = new Inventory();
        inventory.setProductNumber(productNumber);
        inventory.setAvailableQuantity(quantity);
        inventory.setReservedQuantity(0);
        inventoryRepository.save(inventory);

        log.info("Utworzono inventory dla produktu {} z ilością {}", productNumber, quantity);
    }

    /**
     * Kafka listener - obsługa aktualizacji produktu.
     */
    @KafkaListener(topics = "product-update-topic", groupId = "inventory-service-group")
    @Transactional
    public void handleProductUpdateEvent(ProductEvent event) {
        log.info("Otrzymano event aktualizacji produktu: {}", event);

        String productNumber = event.productNumber();
        int newQuantity = event.quantity();

        Inventory inventory = inventoryRepository.findByProductNumber(productNumber)
                .orElseThrow(() -> new RuntimeException("Produkt nie istnieje w magazynie: " + productNumber));

        inventory.setAvailableQuantity(newQuantity);
        inventoryRepository.save(inventory);

        log.info("Zaktualizowano inventory dla produktu {} - nowa ilość: {}", productNumber, newQuantity);
    }
}
