package pl.electricshop.product_service.api;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.electricshop.product_service.api.dto.response.ProductResponse;
import pl.electricshop.product_service.service.ProductService;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public ResponseEntity<ProductResponse> getAllProducts(
            @RequestParam(defaultValue = "0") Integer pageNumber,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(defaultValue = "productName") String sortBy,
            @RequestParam(defaultValue = "asc") String sortOrder,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category) {

        ProductResponse response = productService.getAllProducts(
                pageNumber, pageSize, sortBy, sortOrder, keyword, category);
        return ResponseEntity.ok(response);
    }
}
