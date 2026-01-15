package pl.electricshop.product_service.service;

import pl.electricshop.product_service.api.dto.CategoryDTO;
import pl.electricshop.product_service.api.dto.response.CategoryResponse;

public interface CategoryService {

    CategoryResponse getAllCategories(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder);

    CategoryDTO createCategory(CategoryDTO categoryDTO);
}
