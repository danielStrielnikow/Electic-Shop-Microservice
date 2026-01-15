package pl.electricshop.product_service.mapper;

import org.mapstruct.Mapper;
import pl.electricshop.product_service.api.dto.CategoryDTO;
import pl.electricshop.product_service.model.Category;

/**
 * Mapper for Product entity <-> DTO conversions
 */
@Mapper(componentModel = "spring")
public interface CategoryMapper {

    CategoryDTO toResponseDTO(Category category);

    Category toEntityDTO(CategoryDTO categoryDTO);
}
