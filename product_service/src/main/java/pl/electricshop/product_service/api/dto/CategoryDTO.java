package pl.electricshop.product_service.api.dto;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CategoryDTO {
    private UUID categoryId;
    private String categoryName;
}
