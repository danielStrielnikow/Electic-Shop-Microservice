package pl.electricshop.product_service.model;

import jakarta.persistence.*;
import lombok.*;
import pl.electricshop.product_service.base.BaseEntity;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "categories")
public class Category extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private Long categoryId;

    private String categoryName;

    @ManyToMany(mappedBy = "categories")
    private Set<Product> products = new HashSet<>();
}
