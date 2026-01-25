package pl.electricshop.common.events.payment;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemPayload {
    private String productNumber;
    private String productName;
    private Integer quantity;
    private Double unitPrice;
    private Double totalLinePrice; // quantity * unitPrice
}
