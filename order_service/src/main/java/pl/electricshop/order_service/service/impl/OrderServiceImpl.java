package pl.electricshop.order_service.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import pl.electricshop.common.events.cart.CartCheckoutEvent;
import pl.electricshop.common.events.payment.OrderCreatedEvent;
import pl.electricshop.common.events.payment.OrderItemPayload;
import pl.electricshop.common.events.payment.OrderPlacedEvent;
import pl.electricshop.order_service.api.AddressDTO;
import pl.electricshop.order_service.mapper.OrderMapper;
import pl.electricshop.order_service.model.Order;
import pl.electricshop.order_service.model.enums.OrderStatus;
import pl.electricshop.order_service.repository.OrderItemRepository;
import pl.electricshop.order_service.repository.OrderRepository;
import pl.electricshop.order_service.service.OrderService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final UserServiceClient userClient;
    private final OrderMapper orderMapper;

    /**
     * Główna metoda "Place Order".
     * Nie jest wywoływana przez REST Controller, ale przez KAFKĘ z Cart Service!
     */
    @Override
    @Transactional
    @KafkaListener(topics = "cart-checkout-topic", groupId = "order-group")
    public void proccessOrder(CartCheckoutEvent event) {
        log.info("Rozpoczynam tworzenie zamówienia dla: {}", event.getEmail());

        AddressDTO addressDTO = fetchAddressOrThrow(event.getAddressId());

        boolean isReserved = inventoryClient.reserveProducts(event.getItems());
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
                order.getTotalAmount()
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
                        item.getProductId().toString(), // lub productNumber jeśli masz
                        item.getProductName(),
                        item.getQuantity(),
                        item.getOrderedProductPrice(),
                        item.getOrderedProductPrice() * item.getQuantity()
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
}
