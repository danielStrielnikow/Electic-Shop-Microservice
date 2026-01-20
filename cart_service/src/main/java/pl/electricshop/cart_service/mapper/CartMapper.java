package pl.electricshop.cart_service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import pl.electricshop.cart_service.dto.CartResponse;
import pl.electricshop.cart_service.dto.CartResponse.CartItemResponse;
import pl.electricshop.cart_service.model.Cart;
import pl.electricshop.cart_service.model.CartItem;

import java.math.BigDecimal;
import java.util.List;

@Mapper(componentModel = "spring")
public interface CartMapper {

    @Mapping(target = "totalItems", expression = "java(calculateTotalItems(cart))")
    CartResponse toResponse(Cart cart);

    List<CartItemResponse> toItemResponseList(List<CartItem> items);

    @Mapping(source = "productPrice", target = "unitPrice")
    @Mapping(target = "totalPrice", expression = "java(calculateItemTotal(item))")
    CartItemResponse toItemResponse(CartItem item);

    default int calculateTotalItems(Cart cart) {
        if (cart.getItems() == null) return 0;
        return cart.getItems().stream()
                .mapToInt(CartItem::getQuantity)
                .sum();
    }

    default Double calculateItemTotal(CartItem item) {
        double price = item.getProductPrice() != null ? item.getProductPrice() : 0.0;
        double discount = item.getDiscount() != null ? item.getDiscount() : 0.0;
        return (price * item.getQuantity()) - discount;
    }
}
