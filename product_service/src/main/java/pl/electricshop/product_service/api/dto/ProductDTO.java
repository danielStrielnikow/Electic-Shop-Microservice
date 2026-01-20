package pl.electricshop.product_service.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductDTO {
    private String productNumber;

    @NotBlank(message = "Product name is required")
    private String productName;

    private String image;

    @NotBlank(message = "Description is required")
    @Size(min = 6, message = "Description must contain at least 6 characters")
    private String description;

    @Positive(message = "Price must be positive")
    private BigDecimal price;

    private BigDecimal discount;

    private BigDecimal specialPrice;

    @Positive(message = "Quantity must be positive")
    private Integer quantity;
}
