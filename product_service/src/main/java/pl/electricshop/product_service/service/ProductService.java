package pl.electricshop.product_service.service;


import jakarta.transaction.Transactional;
import org.springframework.web.multipart.MultipartFile;
import pl.electricshop.product_service.api.dto.ProductDTO;
import pl.electricshop.product_service.api.dto.response.ProductResponse;
public interface ProductService {

    ProductResponse getAllProducts(Integer pageNumber, Integer pageSize, String sortBy,
                                   String sortOrder, String keyword, String category);

//    ProductResponse searchByCategory(String categoryNumber, Integer pageNumber,
//                                            Integer pageSize, String sortBy, String sortOrder);
//
//    ProductResponse searchProductByKeyWord(String keyword, Integer pageNumber,
//                                           Integer pageSize, String sortBy, String sortOrder);

    ProductDTO addProduct(String categoryNumber, ProductDTO productDTO);


    @Transactional
    ProductDTO updateProduct(String productNumber, ProductDTO productDTO);

    @Transactional
    ProductDTO updateProductImage(String productNumber, MultipartFile image);

    @Transactional
    void deleteProductById(String productNumber);


}
