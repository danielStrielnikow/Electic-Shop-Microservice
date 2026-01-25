package pl.electricshop.order_service.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pl.electricshop.common.events.base.BaseEntity;

import java.math.BigDecimal;

@Entity
@Table(name = "order_item")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderItem extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    private String productNumber;      // ID produktu (do komunikacji z Inventory/Product service)
    private String productName;

    private Integer quantity;
    private BigDecimal discount;
    private BigDecimal orderedProductPrice;
}
