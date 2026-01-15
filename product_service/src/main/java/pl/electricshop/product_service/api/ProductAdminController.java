package pl.electricshop.product_service.api;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.electricshop.product_service.api.dto.ProductDTO;
import pl.electricshop.product_service.service.ProductService;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/products")
@RequiredArgsConstructor
public class ProductAdminController {
    private final ProductService productService;

    @PostMapping("/categories/{categoryId}/products")
    public ResponseEntity<ProductDTO> createProduct(@Valid @RequestBody ProductDTO product,
                                                    @PathVariable UUID categoryId) {
        ProductDTO savedProduct = productService.addProduct(categoryId, product);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedProduct);
    }
}
