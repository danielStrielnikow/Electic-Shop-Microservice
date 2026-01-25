package pl.electricshop.order_service.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pl.electricshop.common.events.cart.CartCheckoutEvent;
import pl.electricshop.common.events.cart.CartItemPayload;
import pl.electricshop.order_service.api.AddressDTO;
import pl.electricshop.order_service.model.AddressSnapshot;
import pl.electricshop.order_service.model.Order;
import pl.electricshop.order_service.model.OrderItem;
import pl.electricshop.order_service.model.enums.OrderStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class OrderMapper {

    /**
     * Główna metoda wrappera.
     * Bierze Event (z Kafki) + Adres (z Feign) -> Zwraca gotową Encję.
     */
    public Order createOrderEntity(CartCheckoutEvent event, AddressDTO addressDTO) {
        Order order = new Order();

        // 1. Podstawowe dane
        order.setEmail(event.getEmail());
        order.setOrderDate(LocalDateTime.now());
        order.setTotalAmount(event.getTotalPrice());
        order.setOrderStatus(OrderStatus.PENDING);

        // 2. Snapshot Adresu (Delegujemy do metody prywatnej, żeby było czytelnie)
        order.setAddressSnapshot(mapToAddressSnapshot(addressDTO));

        // 3. Pozycje zamówienia (Items)
        List<OrderItem> orderItems = mapToOrderItems(event.getItems(), order);
        order.setOrderItems(orderItems);

        return order;
    }

    // Metoda pomocnicza - tworzy snapshot adresu
    private AddressSnapshot mapToAddressSnapshot(AddressDTO dto) {
        AddressSnapshot snapshot = new AddressSnapshot();
        snapshot.setStreet(dto.getStreet());
        snapshot.setCity(dto.getCity());
        snapshot.setZipCode(dto.getZipCode());
        snapshot.setCountry(dto.getCountry());
        snapshot.setBuildingName(dto.getBuildingName());
        snapshot.setOriginalAddressId(dto.getAddressId());
        return snapshot;
    }

    // Metoda pomocnicza - mapuje listę przedmiotów
    private List<OrderItem> mapToOrderItems(List<CartItemPayload> itemsPayload, Order order) {
        return itemsPayload.stream()
                .map(payload -> {
                    OrderItem item = new OrderItem();
                    item.setProductId(payload.getProductId());
                    item.setProductName(payload.getProductName()); // Snapshot nazwy
                    item.setOrderedProductPrice(payload.getPrice()); // Snapshot ceny
                    item.setQuantity(payload.getQuantity());
                    item.setDiscount(payload.getDiscount());
                    item.setOrder(order); // Ustawiamy relację dwukierunkową
                    return item;
                })
                .collect(Collectors.toList());
    }
}
