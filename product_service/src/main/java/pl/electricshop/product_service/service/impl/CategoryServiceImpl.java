package pl.electricshop.product_service.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import pl.electricshop.product_service.api.dto.CategoryDTO;
import pl.electricshop.product_service.api.dto.response.CategoryResponse;
import pl.electricshop.product_service.errors.AppError;
import pl.electricshop.product_service.exception.APIException;
import pl.electricshop.product_service.mapper.CategoryMapper;
import pl.electricshop.product_service.model.Category;
import pl.electricshop.product_service.repository.CategoryRepository;
import pl.electricshop.product_service.service.CategoryService;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;



    @Override
    public CategoryResponse getAllCategories(Integer pageNumber, Integer pageSize,
                                             String sortBy, String sortOrder) {
        Pageable pageable = PageRequest.of(
                pageNumber,
                pageSize,
                Sort.by(Sort.Direction.fromString(sortOrder), sortBy)
        );

        Page<Category> categoryPage = categoryRepository.findAll(pageable);

        return getCategoryResponse(categoryPage);
    }

    @Override
    public CategoryDTO createCategory(CategoryDTO categoryDTO) {
        if (categoryRepository.findByCategoryName(categoryDTO.getCategoryName()) != null) {
            throw new APIException(AppError.ERROR_CATEGORY_EXISTS);
        }

        Category savedCategory = categoryRepository.save(categoryMapper.toEntity(categoryDTO));
        return categoryMapper.toDTO(savedCategory);
    }

    @Override
    public CategoryDTO updateCategory(CategoryDTO categoryDTO, String categoryNumber) {
        Category existingCategory = fetchCategoryById(categoryNumber);

        existingCategory.setCategoryName(categoryDTO.getCategoryName());
        Category updatedCategory = categoryRepository.save(existingCategory);
        return categoryMapper.toDTO(updatedCategory);
    }



    @Override
    public void deleteCategoryById(String categoryNumber) {
        Category existingCategory = fetchCategoryById(categoryNumber);
        categoryRepository.delete(existingCategory);
    }

    private Category fetchCategoryById(String categoryNumber) {
        return categoryRepository.findByCategoryNumber(categoryNumber)
                .orElseThrow(() -> new APIException(AppError.ERROR_CATEGORY_NOT_FOUND));
    }

    private CategoryResponse getCategoryResponse(Page<Category> categoryPage) {
        if (categoryPage.isEmpty()) {
            throw new APIException(AppError.ERROR_CATEGORY_NOT_FOUND);
        }

        List<CategoryDTO> categoryDTOS = categoryPage.getContent()
                .stream()
                .map(categoryMapper::toDTO)
                .toList();

        return new CategoryResponse(
                categoryDTOS,
                categoryPage.getNumber(),
                categoryPage.getSize(),
                categoryPage.getTotalElements(),
                categoryPage.getTotalPages(),
                categoryPage.isLast()
        );
    }
}
