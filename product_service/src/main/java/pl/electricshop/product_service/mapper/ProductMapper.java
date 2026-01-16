package pl.electricshop.product_service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import pl.electricshop.product_service.api.dto.ProductDTO;
import pl.electricshop.product_service.model.Product;

/**
 * Mapper for Product entity <-> DTO conversions
 */
@Mapper(componentModel = "spring")
public interface ProductMapper {

    ProductDTO toDTO(Product product);

    @Mapping(target = "uuid", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "categories", ignore = true)
    @Mapping(target = "productNumber", ignore = true)
    Product toEntity(ProductDTO productDTO);

    @Mapping(target = "uuid", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "productNumber", ignore = true)
    void updateEntityFromDTO(ProductDTO productDTO, Product product);
}
