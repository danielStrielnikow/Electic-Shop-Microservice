package pl.electricshop.order_service.mapper;

import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import pl.electricshop.common.events.cart.CartCheckoutEvent;
import pl.electricshop.common.events.cart.CartItemPayload;
import pl.electricshop.order_service.api.AddressDTO;
import pl.electricshop.order_service.model.AddressSnapshot;
import pl.electricshop.order_service.model.Order;
import pl.electricshop.order_service.model.OrderItem;

import java.time.LocalDateTime;

@Mapper(componentModel = "spring", imports = {LocalDateTime.class})
public interface OrderMapper {

    /**
     * Główna metoda mapująca.
     */
    // ZMIANA 1: Twój ID w BaseEntity to 'uuid', a nie 'orderId'
    @Mapping(target = "uuid", ignore = true)

    @Mapping(target = "email", source = "event.email")
    @Mapping(target = "totalAmount", source = "event.totalPrice")

    @Mapping(target = "orderItems", source = "event.items")

    // ZMIANA 2: W encji pole nazywa się 'addressSnapshot', a nie 'shippingAddress'
    @Mapping(target = "addressSnapshot", source = "addressDTO")

    // ZMIANA 3: Upewnij się, że Twój enum OrderStatus ma wartość 'PENDING'
    // Jeśli nie, zmień to np. na constant = "CREATED"
    @Mapping(target = "orderStatus", constant = "PENDING")

    @Mapping(target = "orderDate", expression = "java(LocalDateTime.now())")

    // Ignorujemy pola, których nie ustawiamy w tym momencie
    @Mapping(target = "orderNumber", ignore = true)
    @Mapping(target = "paymentId", ignore = true)
    @Mapping(target = "userId", source = "event.userId")
    Order createOrderEntity(CartCheckoutEvent event, AddressDTO addressDTO);

    /**
     * Helper 1: Mapowanie Adresu
     */
    @Mapping(target = "originalAddressId", source = "addressId")
    AddressSnapshot mapToAddressSnapshot(AddressDTO dto);

    /**
     * Helper 2: Mapowanie Itemu
     */
    @Mapping(target = "uuid", ignore = true)
    @Mapping(target = "order", ignore = true)
    @Mapping(target = "orderedProductPrice", source = "price")
    OrderItem mapToOrderItem(CartItemPayload payload);

    @AfterMapping
    default void linkOrderItems(@MappingTarget Order order) {
        if (order.getOrderItems() != null) {
            order.getOrderItems().forEach(item -> item.setOrder(order));
        }
    }
}