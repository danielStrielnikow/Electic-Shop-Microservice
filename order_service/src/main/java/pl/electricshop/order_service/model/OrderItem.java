package pl.electricshop.order_service.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pl.electricshop.common.events.base.BaseEntity;

@Entity
@Table
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderItem extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    private Long productId;      // ID produktu (do komunikacji z Inventory/Product service)
    private String productName;

    private Integer quantity;
    private double discount;
    private double orderedProductPrice;
}
