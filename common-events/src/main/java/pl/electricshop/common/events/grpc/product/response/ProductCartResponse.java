package pl.electricshop.common.events.grpc.product.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProductCartResponse {
    private String productNumber;
    private String productName;
    private Double price;
    private String image;
    private Double discount;
    private Double specialPrice;
    private Double quantity;
}
