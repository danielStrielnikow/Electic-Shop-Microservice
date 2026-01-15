package pl.electricshop.product_service.model;

import com.aventrix.jnanoid.jnanoid.NanoIdUtils;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pl.electricshop.product_service.base.BaseEntity;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "categories")
public class Category extends BaseEntity {

    @Column(nullable = false, unique = true, name = "category_number")
    private String categoryNumber;

    @Column(name = "category_name")
    private String categoryName;

    @ManyToMany(mappedBy = "categories")
    private Set<Product> products = new HashSet<>();

    @PrePersist
    public void generateCategoryNumber() {
        if (this.categoryNumber == null) {
            this.categoryNumber = "CAT-" + NanoIdUtils.randomNanoId(
                    NanoIdUtils.DEFAULT_NUMBER_GENERATOR,
                    "0123456789ABCDEF".toCharArray(),
                    6
            );
        }
    }
}
