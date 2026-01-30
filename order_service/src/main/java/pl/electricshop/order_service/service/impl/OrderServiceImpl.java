package pl.electricshop.order_service.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import pl.electricshop.common.events.cart.CartCheckoutEvent;
import pl.electricshop.common.events.payment.*;
import pl.electricshop.order_service.api.AddressDTO;
import pl.electricshop.order_service.client.UserServiceClient;
import pl.electricshop.order_service.mapper.OrderMapper;
import pl.electricshop.order_service.model.Order;
import pl.electricshop.order_service.model.enums.OrderStatus;
import pl.electricshop.order_service.repository.OrderRepository;
import pl.electricshop.order_service.service.InventoryClient;
import pl.electricshop.order_service.service.OrderService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final UserServiceClient userClient;
    private final OrderMapper orderMapper;
    private final InventoryClient inventoryClient;

    /**
     * Główna metoda "Place Order".
     * Nie jest wywoływana przez REST Controller, ale przez KAFKĘ z Cart Service!
     */
    @Override
    @Transactional
    @KafkaListener(topics = "cart-checkout-topic", groupId = "order-group")
    public void proccessOrder(CartCheckoutEvent event) {
        log.info("Rozpoczynam tworzenie zamówienia dla: {}", event.getEmail());
        // 1. Uruchamiamy pobieranie adresu i rezerwację wirtualnych wątkach
        CompletableFuture<AddressDTO> addressFuture = CompletableFuture.supplyAsync(
                () -> fetchAddressOrThrow(event.getAddressId())
        );

        CompletableFuture<Boolean> inventoryFuture = CompletableFuture.supplyAsync(
                () -> inventoryClient.reserveProducts(event.getItems())
        );

        // 2. Czekamy na oba wyniki (Join)
        AddressDTO addressDTO = addressFuture.join();
        boolean isReserved = inventoryFuture.join();

        if (!isReserved) {
            // TODO: Wyślij event OrderFailedEvent
            log.error("Brak towaru na stanie! Zamówienie odrzucone: {}", event.getEmail());
            throw new RuntimeException("Nie można zarezerwować produktów.");
        }


        Order order = orderMapper.createOrderEntity(event, addressDTO);

        Order savedOrder = orderRepository.save(order);

        notifyPaymentService(savedOrder);

        log.info("Zamówienie utworzone pomyślnie dla: {}, ID: {}", event.getEmail(), savedOrder.getUuid());
    }

    private AddressDTO fetchAddressOrThrow(UUID addressId) {
        try {
            return userClient.getAddressById(addressId);
        } catch (Exception e) {
            log.error("Błąd podczas pobierania adresu IDL {}", addressId, e);
            throw new RuntimeException("Nie można pobrać adresu użytkownika.");
        }
    }

    private void notifyPaymentService(Order order) {
        OrderCreatedEvent paymentEvent = new OrderCreatedEvent(
                order.getUuid(),
                order.getEmail(),
                order.getTotalAmount(),
                "BLIK",
                "PLN"
        );

        log.info("Wysyłam zdarzenie OrderCreatedEvent do usługi płatności dla zamówienia: {}", order.getUuid());
        kafkaTemplate.send("order-created-topic", paymentEvent);
    }

    // W OrderService (po otrzymaniu potwierdzenia płatności)

    public void finalizeOrder(UUID orderId) {
        Order order = orderRepository.findById(orderId).orElseThrow();

        // 1. Mapowanie pozycji zamówienia na Payload (dla maila)
        List<OrderItemPayload> itemPayloads = order.getOrderItems().stream()
                .map(item -> new OrderItemPayload(
                        item.getProductNumber(),
                        item.getProductName(),
                        item.getQuantity(),
                        item.getOrderedProductPrice(),
                        item.getOrderedProductPrice()
                                .multiply(BigDecimal.valueOf(item.getQuantity()))
                ))
                .collect(Collectors.toList());

        // 2. Tworzenie Eventu
        OrderPlacedEvent event = new OrderPlacedEvent(
                order.getUuid(),
                order.getUserId(),
                order.getEmail(),
                order.getTotalAmount(),
                itemPayloads,
                order.getAddressSnapshot().getCity(),
                order.getAddressSnapshot().getStreet(),
                LocalDateTime.now());
        order.setOrderStatus(OrderStatus.DELIVERED);
        orderRepository.save(order);
        // 3. Wysłanie
        kafkaTemplate.send("order-placed-topic", event);
    }

    @KafkaListener(topics = "payment-succeeded-topic", groupId = "order-group")
    public void handlePaymentSucceeded(PaymentSucceededEvent event) {
        log.info("Otrzymano potwierdzenie płatności: {}", event.orderId());
        CompletableFuture.runAsync(() -> {
            try {
                finalizeOrder(event.orderId());
                log.info("Zamówienie {} zostało sfinalizowane po płatności.", event.orderId());
            } catch (Exception e) {
                log.error("Błąd podczas finalizacji zamówienia po płatności: {}", event.orderId(), e);
                // Tutaj można dodać mechanizm ponawiania (retry)
            }
        });
    }

    @KafkaListener(topics = "payment-failed-topic", groupId = "order-group")
    @Transactional
    public void handlePaymentFailed(PaymentFailedEvent event) {
        log.warn("Płatność nieudana dla zamówienia: {}. Powód: {}", event.orderId(), event.errorMessage());

        orderRepository.findById(event.orderId()).ifPresent(order -> {
            // 1. Zmiana statusu
            order.setOrderStatus(OrderStatus.PAYMENT_FAILED);
            orderRepository.save(order);

            // 2. Wysłanie eventu do Inventory, aby zwolnić blokadę (jeśli była)
            OrderFailedEvent failedEvent = new OrderFailedEvent(order.getUuid(),
                    order.getOrderItems().stream()
                            .map(item -> new OrderItemPayload(
                                    item.getProductNumber(),
                                    item.getProductName(),
                                    item.getQuantity(),
                                    item.getOrderedProductPrice(),
                                    item.getOrderedProductPrice()
                                            .multiply(BigDecimal.valueOf(item.getQuantity()))
                            ))
                            .collect(Collectors.toList()),
                    "PAYMENT_REJECTED",
                    order.getEmail()
            );
            kafkaTemplate.send("order-failed-topic", failedEvent);

            log.info("Wysłano powiadomienie o anulowaniu rezerwacji dla zamówienia: {}", order.getUuid());
        });
    }
}
