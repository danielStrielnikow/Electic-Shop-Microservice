package pl.electricshop.product_service.model;

import com.aventrix.jnanoid.jnanoid.NanoIdUtils;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import pl.electricshop.product_service.base.BaseEntity;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "products")
@ToString
public class Product extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID productId;

    @Column(nullable = false, unique = true, name = "product_number")
    private String productNumber;

    private String productName;

    private String image;

    @NotBlank
    @Size(min = 6, message = "Description must contain at least 6 characters")
    private String description;

    private double price;
    private double discount;
    private double specialPrice;


    @ManyToMany
    @JoinTable(
            name = "product_categories",
            joinColumns = @JoinColumn(name = "product_id"),
            inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    private Set<Category> categories = new HashSet<>();


    @PrePersist
    public void generateId() {
        if (this.productNumber == null) {
            this.productNumber = "EL-" + NanoIdUtils.randomNanoId(
                    NanoIdUtils.DEFAULT_NUMBER_GENERATOR,
                    "0123456789ABCDEF".toCharArray(),
                    6
            );
        }
    }
}
