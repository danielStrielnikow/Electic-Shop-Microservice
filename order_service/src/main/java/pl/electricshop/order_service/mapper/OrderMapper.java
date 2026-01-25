package pl.electricshop.order_service.mapper;

import lombok.RequiredArgsConstructor;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
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

@Mapper(componentModel = "spring", imports = {LocalDateTime.class})
public interface OrderMapper {

    /**
     * Główna metoda mapująca.
     * Bierze DWA źródła (Event i AddressDTO) i skleja w jeden Order.
     */
    @Mapping(target = "orderId", ignore = true) // Generowane przez bazę
    @Mapping(target = "email", source = "event.email")
    @Mapping(target = "totalAmount", source = "event.totalPrice")

    // Mapowanie listy itemów (automatycznie użyje metody mapToOrderItem poniżej)
    @Mapping(target = "orderItems", source = "event.items")

    // Mapowanie adresu (automatycznie użyje metody mapToAddressSnapshot poniżej)
    @Mapping(target = "shippingAddress", source = "addressDTO")

    // Stałe wartości i wyrażenia Java
    @Mapping(target = "orderStatus", constant = "PENDING")
    @Mapping(target = "orderDate", expression = "java(LocalDateTime.now())")
    Order createOrderEntity(CartCheckoutEvent event, AddressDTO addressDTO);

    /**
     * Helper 1: Mapowanie Adresu (DTO -> Embedded Snapshot)
     * MapStruct sam domyśli się, że city -> city, street -> street.
     * Musimy tylko obsłużyć ID.
     */
    @Mapping(target = "originalAddressId", source = "addressId")
    AddressSnapshot mapToAddressSnapshot(AddressDTO dto);

    /**
     * Helper 2: Mapowanie pojedynczego przedmiotu (DTO -> Entity)
     * CartItemPayload -> OrderItem
     */
    @Mapping(target = "orderItemId", ignore = true)
    @Mapping(target = "order", ignore = true) // Ustawimy to w @AfterMapping
    @Mapping(target = "orderedProductPrice", source = "price") // Różne nazwy pól
    OrderItem mapToOrderItem(CartItemPayload payload);

    /**
     * MAGIA: Dwukierunkowe relacje (@AfterMapping)
     * MapStruct stworzy listę OrderItems, ale każdy item będzie miał pole 'order' = null.
     * Ta metoda uruchamia się na końcu i naprawia relację rodzic-dziecko.
     */
    @AfterMapping
    default void linkOrderItems(@MappingTarget Order order) {
        if (order.getOrderItems() != null) {
            order.getOrderItems().forEach(item -> item.setOrder(order));
        }
    }
}
