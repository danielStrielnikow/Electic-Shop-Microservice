package pl.electricshop.product_service.api;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pl.electricshop.product_service.api.dto.response.CategoryResponse;
import pl.electricshop.product_service.service.CategoryService;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
public class CategoryController {
    private final CategoryService categoryService;

    @GetMapping()
    public ResponseEntity<CategoryResponse> getAllCategories(
            @RequestParam(defaultValue = "0") Integer pageNumber,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(defaultValue = "productName") String sortBy,
            @RequestParam(defaultValue = "asc") String sortOrder)
     {
         CategoryResponse allCategories = categoryService.getAllCategories(pageNumber, pageSize, sortBy, sortOrder);
         return ResponseEntity.ok(allCategories);
    }
}
