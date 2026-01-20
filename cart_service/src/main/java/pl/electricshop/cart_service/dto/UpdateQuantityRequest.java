package pl.electricshop.cart_service.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateQuantityRequest {

    @NotBlank(message = "Product number is required")
    private String productNumber;

    @Min(value = 1, message = "Quantity must be at least 1")
    private int quantity;
}
