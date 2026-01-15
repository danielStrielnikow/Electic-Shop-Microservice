package pl.electricshop.product_service.api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import pl.electricshop.product_service.api.dto.ProductDTO;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class ProductResponse {
    private List<ProductDTO> content;
    private Integer pageNumber;
    private Integer pageSize;
    private Long totalElements;
    private Integer totalPages;
    private boolean lastPage;

    public ProductResponse(List<ProductDTO> content) {
        this.content = content;
    }
}
