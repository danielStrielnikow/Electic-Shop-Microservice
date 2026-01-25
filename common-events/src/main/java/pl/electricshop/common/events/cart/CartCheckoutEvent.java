package pl.electricshop.common.events.cart;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CartCheckoutEvent {
    private String email;
    private Long addressId;     // Tylko ID!
    private Double totalPrice;
    private List<CartItemPayload> items;
}
