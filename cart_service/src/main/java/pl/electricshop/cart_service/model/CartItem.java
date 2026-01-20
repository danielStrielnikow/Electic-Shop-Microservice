package pl.electricshop.cart_service.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Element koszyka - przechowuje snapshot danych produktu w momencie dodania.
 * Klasa przechowywana w Redis jako część Cart.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CartItem implements Serializable {

    private String productNumber;
    private String productName;
    private Integer quantity;
    private Double discount;
    private Double productPrice;
}
