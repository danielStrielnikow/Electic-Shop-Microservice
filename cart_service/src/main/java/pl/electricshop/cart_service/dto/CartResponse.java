package pl.electricshop.cart_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartResponse {

    private UUID userId;
    private List<CartItemResponse> items;
    private BigDecimal totalPrice;
    private int totalItems;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CartItemResponse {
        private String productNumber;
        private String productName;
        private int quantity;
        private Double unitPrice;
        private Double discount;
        private Double totalPrice;
    }
}
