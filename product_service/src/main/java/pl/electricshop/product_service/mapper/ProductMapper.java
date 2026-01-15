package pl.electricshop.product_service.mapper;

import org.mapstruct.Mapper;
import pl.electricshop.product_service.api.dto.ProductDTO;
import pl.electricshop.product_service.model.Product;

/**
 * Mapper for Product entity <-> DTO conversions
 */
@Mapper(componentModel = "spring")
public interface ProductMapper {

    ProductDTO toResponseDTO(Product product);

    Product toEntityDTO(ProductDTO productDTO);
}
