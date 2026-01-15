package pl.electricshop.product_service.service;


import jakarta.transaction.Transactional;
import org.springframework.web.multipart.MultipartFile;
import pl.electricshop.product_service.api.dto.ProductDTO;
import pl.electricshop.product_service.api.dto.response.ProductResponse;

import java.util.UUID;
public interface ProductService {

    ProductResponse getAllProducts(Integer pageNumber, Integer pageSize, String sortBy,
                                   String sortOrder, String keyword, String category);

    ProductResponse searchByCategory(UUID categoryId, Integer pageNumber,
                                            Integer pageSize, String sortBy, String sortOrder);

    ProductResponse searchProductByKeyWord(String keyword, Integer pageNumber,
                                           Integer pageSize, String sortBy, String sortOrder);

    ProductDTO addProduct(String categoryNumber, ProductDTO productDTO);


    @Transactional
    ProductDTO updateProduct(Long productId, ProductDTO productDTO);

    @Transactional
    ProductDTO updateProductImage(Long productId, MultipartFile image);

    @Transactional
    ProductDTO deleteProductById(Long productId);


}
