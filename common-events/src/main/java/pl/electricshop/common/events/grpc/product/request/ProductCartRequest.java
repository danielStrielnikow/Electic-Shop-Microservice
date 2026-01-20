package pl.electricshop.common.events.grpc.product.request;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProductCartRequest {
    private String productNumber;
}
