package pl.electricshop.inventory_service.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import pl.electricshop.common.events.base.BaseEntity;

@Entity
@Table(name = "inventory")
@Getter
@Setter
public class Inventory extends BaseEntity {

    private String productNumber;
    private Integer availableQuantity;
    private Integer reservedQuantity;
}
