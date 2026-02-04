package pl.electricshop.common.events.payment;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemPayload {
    private String productNumber;
    private String productName;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal totalLinePrice; // quantity * unitPrice
}
