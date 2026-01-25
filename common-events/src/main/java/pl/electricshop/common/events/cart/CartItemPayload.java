package pl.electricshop.common.events.cart;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CartItemPayload {
    private String productNumber;      // Tylko ID
    private String productName;  // Snapshot nazwy
    private Integer quantity;    // Ile sztuk
    private BigDecimal price;        // Snapshot ceny za sztukę
    private BigDecimal discount;     // Ewentualny rabat
}
