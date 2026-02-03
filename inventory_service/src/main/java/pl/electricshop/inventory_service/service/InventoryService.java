package pl.electricshop.inventory_service.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import pl.electricshop.common.events.payment.OrderFailedEvent;
import pl.electricshop.common.events.payment.OrderPlacedEvent;
import pl.electricshop.common.events.product.ProductEvent;
import pl.electricshop.inventory_service.model.Inventory;
import pl.electricshop.inventory_service.model.api.ProductReservationDTO;
import pl.electricshop.inventory_service.repository.InventoryRepository;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.Set;
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

    /**
     * Kafka listener - obsługa usunięcia produktu - Płatność przeszła pomyślnie.
     */
    @KafkaListener(topics = "order-placed-topic", groupId = "inventory-service-group")
    @Transactional
    public void handleOrderPlacedEvent(OrderPlacedEvent event) {
        log.info("Otrzymano event złożenia zamówienia: {}", event);

        event.getItems().forEach(
                item -> {
                    String productNumber = item.getProductNumber();
                    int quantity = item.getQuantity();

                    Inventory inventory = inventoryRepository.findByProductNumber(productNumber)
                            .orElseThrow(() -> new RuntimeException("Produkt nie istnieje w magazynie: " + productNumber));

                    // Zmniejsz zarezerwowaną ilość
                    inventory.setReservedQuantity(Math.max(0, inventory.getReservedQuantity() - quantity));
                    inventoryRepository.save(inventory);

                    log.info("Zamówienie zrealizowane - zaktualizowano inventory dla produktu {}: zarezerwowana ilość zmniejszona o {}",
                            productNumber, quantity);
                }

        );
    }

    @KafkaListener(topics = "order-failed-topic", groupId = "inventory-service-group")
    @Transactional
    public void handleOrderFailed(OrderFailedEvent event) {
        log.info("Odebrano OrderFailedEvent dla zamówienia {}. Zwalniam rezerwację.", event);

        event.items().forEach(
                item -> {
                    inventoryRepository.releaseStock(item.getProductNumber(), item.getQuantity());
                }
        );
    }

    /**
     * Aktualizuje rezerwację - zmienia ilość zarezerwowanych produktów.
     * Jeśli nowa ilość jest większa - rezerwuje więcej.
     * Jeśli nowa ilość jest mniejsza - zwalnia część rezerwacji.
     *
     * @param reservationId format: "{userId}:{productNumber}"
     * @param newQuantity nowa ilość do zarezerwowania
     * @return faktycznie zarezerwowana ilość lub -1 jeśli błąd
     */
    @Transactional
    public int updateReservation(String reservationId, int newQuantity) {
        String redisKey = "reservation:" + reservationId;
        String currentQuantityStr = redisTemplate.opsForValue().get(redisKey);

        if (currentQuantityStr == null) {
            log.warn("Brak rezerwacji do aktualizacji: {}", reservationId);
            return -1;
        }

        int currentQuantity = Integer.parseInt(currentQuantityStr);

        // Wyciągnij productNumber z reservationId (format: "userId:productNumber")
        String[] parts = reservationId.split(":");
        if (parts.length < 2) {
            log.error("Nieprawidłowy format reservationId: {}", reservationId);
            return -1;
        }
        String productNumber = parts[1];

        int diff = newQuantity - currentQuantity;

        if (diff == 0) {
            log.info("Ilość rezerwacji bez zmian: {}", reservationId);
            return currentQuantity;
        }

        Inventory inventory = inventoryRepository.findByProductNumber(productNumber)
                .orElseThrow(() -> new RuntimeException("Produkt nie istnieje: " + productNumber));

        if (diff > 0) {
            // Trzeba zarezerwować więcej
            if (inventory.getAvailableQuantity() < diff) {
                log.warn("Niewystarczająca ilość do zwiększenia rezerwacji {}. Dostępne: {}, potrzebne: {}",
                        reservationId, inventory.getAvailableQuantity(), diff);
                return -1;
            }
            inventory.setAvailableQuantity(inventory.getAvailableQuantity() - diff);
            inventory.setReservedQuantity(inventory.getReservedQuantity() + diff);
        } else {
            // Trzeba zwolnić część rezerwacji (diff jest ujemny)
            int toRelease = Math.abs(diff);
            inventory.setAvailableQuantity(inventory.getAvailableQuantity() + toRelease);
            inventory.setReservedQuantity(Math.max(0, inventory.getReservedQuantity() - toRelease));
        }

        inventoryRepository.save(inventory);

        // Aktualizuj Redis z nowym TTL
        redisTemplate.opsForValue().set(redisKey, String.valueOf(newQuantity), RESERVATION_TTL);

        log.info("Zaktualizowano rezerwację {}: {} -> {} szt.", reservationId, currentQuantity, newQuantity);
        return newQuantity;
    }

    /**
     * Scheduled job - czyści "osierocone" rezerwacje w bazie danych.
     * Gdy rezerwacja wygaśnie w Redis (po 15 min), baza nadal ma reservedQuantity > 0.
     * Ten job sprawdza produkty z rezerwacjami i weryfikuje czy istnieją odpowiadające klucze w Redis.
     * Jeśli nie - zwalnia reservedQuantity i przywraca availableQuantity.
     *
     * Uruchamiany co minutę.
     */
    @Scheduled(fixedRate = 60000)
    @Transactional
    public void cleanupExpiredReservations() {
        log.debug("Rozpoczynam cleanup wygasłych rezerwacji...");

        // Pobierz wszystkie produkty z niezerową rezerwacją
        List<Inventory> productsWithReservations = inventoryRepository.findAllWithReservations();

        if (productsWithReservations.isEmpty()) {
            log.debug("Brak produktów z aktywnymi rezerwacjami.");
            return;
        }

        for (Inventory inventory : productsWithReservations) {
            String productNumber = inventory.getProductNumber();

            // Szukaj wszystkich kluczy rezerwacji dla tego produktu
            Set<String> keys = redisTemplate.keys("reservation:*:" + productNumber);

            if (keys == null || keys.isEmpty()) {
                // Brak aktywnych rezerwacji w Redis - przywróć stock
                int orphanedQuantity = inventory.getReservedQuantity();
                if (orphanedQuantity > 0) {
                    log.info("Znaleziono osieroconą rezerwację dla produktu {}: {} szt. Przywracam dostępność.",
                            productNumber, orphanedQuantity);

                    inventory.setAvailableQuantity(inventory.getAvailableQuantity() + orphanedQuantity);
                    inventory.setReservedQuantity(0);
                    inventoryRepository.save(inventory);
                }
            } else {
                // Są aktywne rezerwacje - oblicz sumę zarezerwowanych ilości
                int totalReservedInRedis = 0;
                for (String key : keys) {
                    String quantityStr = redisTemplate.opsForValue().get(key);
                    if (quantityStr != null) {
                        totalReservedInRedis += Integer.parseInt(quantityStr);
                    }
                }

                // Jeśli suma w Redis jest mniejsza niż w bazie - zwolnij różnicę
                int dbReserved = inventory.getReservedQuantity();
                if (totalReservedInRedis < dbReserved) {
                    int diff = dbReserved - totalReservedInRedis;
                    log.info("Rozbieżność rezerwacji dla produktu {}: DB={}, Redis={}. Przywracam {} szt.",
                            productNumber, dbReserved, totalReservedInRedis, diff);

                    inventory.setAvailableQuantity(inventory.getAvailableQuantity() + diff);
                    inventory.setReservedQuantity(totalReservedInRedis);
                    inventoryRepository.save(inventory);
                }
            }
        }

        log.debug("Cleanup wygasłych rezerwacji zakończony.");
    }
}

