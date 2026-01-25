package pl.electricshop.common.events.payment;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderPlacedEvent {
    private UUID orderId;
    private UUID userId;
    private String email;

    private Double totalPrice;

    private List<OrderItemPayload> items;

    private String shippingCity;
    private String shippingStreet;

    private LocalDateTime placedAt;
}
