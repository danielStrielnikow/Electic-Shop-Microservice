package pl.electricshop.cart_service.model;

import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Element koszyka - przechowuje snapshot danych produktu w momencie dodania.
 * Klasa przechowywana w Redis jako część Cart.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CartItem implements Serializable {

    private String productNumber;
    private String productName;
    private Integer quantity;
    private Double discount;
    private Double productPrice;
}
