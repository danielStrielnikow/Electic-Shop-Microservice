package pl.electricshop.order_service.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.*;
import pl.electricshop.common.events.base.BaseEntity;

import java.time.LocalDateTime;

@Table(name = "orders")
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Order extends BaseEntity {

    private String orderNumber;

    @Email
    @Column(nullable = false)
    private String email;

    private LocalDateTime orderDate;

    private Double totalAmount;

    private String orderStatus;
}
