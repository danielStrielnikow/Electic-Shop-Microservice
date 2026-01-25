package pl.electricshop.common.events.cart;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CartItemPayload {
    private Long productId;      // Tylko ID
    private String productName;  // Snapshot nazwy
    private Integer quantity;    // Ile sztuk
    private Double price;        // Snapshot ceny za sztukę
    private Double discount;     // Ewentualny rabat
}
