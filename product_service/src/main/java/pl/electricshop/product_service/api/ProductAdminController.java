package pl.electricshop.product_service.api;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
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

    @PutMapping("/{productName}")
    public ResponseEntity<ProductDTO> updateProduct(@Valid @RequestBody ProductDTO productDTO,
                                                    @PathVariable String productName) {
        ProductDTO updatedproductDTO = productService.updateProduct(productName, productDTO);
        return ResponseEntity.status(HttpStatus.OK).body(updatedproductDTO);
    }

    @PutMapping("/{productName}/image")
    public ResponseEntity<ProductDTO> updateProductImage(@PathVariable String productName,
                                                         @RequestParam("image") MultipartFile image) {
        ProductDTO productDTO = productService.updateProductImage(productName, image);
        return ResponseEntity.status(HttpStatus.OK).body(productDTO);
    }

    @DeleteMapping("/{productName}")
    public ResponseEntity<Void> deleteProductById(@PathVariable String productName) {
        productService.deleteProductById(productName);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
