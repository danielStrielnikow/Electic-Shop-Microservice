package pl.electricshop.cart_service.model;

import lombok.*;

import java.io.Serializable;

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
    private String reservationId;  // format: "userId:productNumber"

    public CartItem(String productNumber, String productName, int quantity,
                    double discount, double price) {
        this.productNumber = productNumber;
        this.productName = productName;
        this.quantity = quantity;
        this.discount = discount;
        this.productPrice = price;
    }
}
