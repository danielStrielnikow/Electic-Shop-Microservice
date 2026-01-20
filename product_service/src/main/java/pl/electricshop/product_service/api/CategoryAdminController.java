package pl.electricshop.product_service.api;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.electricshop.product_service.api.dto.CategoryDTO;
import pl.electricshop.product_service.service.CategoryService;

@RestController
@RequestMapping("/api/v1/admin/categories")
@RequiredArgsConstructor
public class CategoryAdminController {
    private final CategoryService categoryService;


    @PostMapping()
    public ResponseEntity<CategoryDTO> createCategory(@Valid @RequestBody CategoryDTO categoryDTO) {
        CategoryDTO savedCategoryDTO = categoryService.createCategory(categoryDTO);
        return  ResponseEntity.status(HttpStatus.CREATED).body(savedCategoryDTO);
    }

    @PutMapping("/{categoryNumber}")
    public ResponseEntity<CategoryDTO> updateCategory(@Valid @RequestBody CategoryDTO categoryDTO,
                                                    @PathVariable String categoryNumber) {
        CategoryDTO updatedCategoryDTO = categoryService.updateCategory(categoryDTO, categoryNumber);
        return ResponseEntity.status(HttpStatus.OK).body(updatedCategoryDTO);
    }

    @DeleteMapping("/{categoryNumber}")
    public ResponseEntity<Void> deleteCategoryById(@PathVariable String categoryNumber) {
        categoryService.deleteCategoryById(categoryNumber);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}

