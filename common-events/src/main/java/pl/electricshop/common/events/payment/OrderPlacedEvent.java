package pl.electricshop.common.events.payment;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderPlacedEvent {
    private String orderId;
    private UUID userId;
    private String email;

    private BigDecimal totalPrice;

    private List<OrderItemPayload> items;

    private String shippingCity;
    private String shippingStreet;

    private String placedAt;
}
