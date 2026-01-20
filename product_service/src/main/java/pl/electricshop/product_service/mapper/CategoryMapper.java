package pl.electricshop.product_service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import pl.electricshop.product_service.api.dto.CategoryDTO;
import pl.electricshop.product_service.model.Category;

/**
 * Mapper for Category entity <-> DTO conversions
 */
@Mapper(componentModel = "spring")
public interface CategoryMapper {

    CategoryDTO toDTO(Category category);

    @Mapping(target = "uuid", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "products", ignore = true)
    @Mapping(target = "categoryNumber", ignore = true) // Generated automatically
    Category toEntity(CategoryDTO categoryDTO);
}
