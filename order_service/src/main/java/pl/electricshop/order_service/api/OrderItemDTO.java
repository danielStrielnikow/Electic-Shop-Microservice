package pl.electricshop.order_service.api;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemDTO {
    private UUID orderItemId;
    private UUID productId;
    private String productName;
    private Integer quantity;
    private double discount;
    private double orderedProductPrice;
}
