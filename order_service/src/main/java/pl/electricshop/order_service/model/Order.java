package pl.electricshop.order_service.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.*;
import pl.electricshop.common.events.base.BaseEntity;
import pl.electricshop.order_service.model.enums.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Table(name = "orders")
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Order extends BaseEntity {

    private UUID userId;

    private String orderNumber;

    @Email
    @Column(nullable = false)
    private String email;

    @OneToMany(mappedBy = "order", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private List<OrderItem> orderItems = new ArrayList<>();

    private LocalDateTime orderDate;

    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    private OrderStatus orderStatus;

    @Embedded
    private AddressSnapshot addressSnapshot;

    private String paymentId;
}
