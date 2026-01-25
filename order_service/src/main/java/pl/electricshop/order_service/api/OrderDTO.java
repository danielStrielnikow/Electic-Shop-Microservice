package pl.electricshop.order_service.api;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderDTO {
    private String OrderNumber;
    private UUID orderId;
    private String email;
    private List<OrderItemDTO> orderItems;
    private LocalDateTime orderDate;
    private Double totalAmount;
    private String orderStatus;


    private AddressDTO shippingAddress;

    private String paymentId;
}
