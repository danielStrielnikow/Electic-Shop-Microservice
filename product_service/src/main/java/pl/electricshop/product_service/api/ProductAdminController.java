package pl.electricshop.product_service.api;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.electricshop.product_service.api.dto.ProductDTO;
import pl.electricshop.product_service.service.ProductService;

@RestController
@RequestMapping("/api/v1/admin/products")
@RequiredArgsConstructor
public class ProductAdminController {
    private final ProductService productService;

    @PostMapping("/categories/{categoryNumber}")
    public ResponseEntity<ProductDTO> createProduct(@Valid @RequestBody ProductDTO product,
                                                    @PathVariable String categoryNumber) {
        ProductDTO savedProduct = productService.addProduct(categoryNumber, product);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedProduct);
    }
}
