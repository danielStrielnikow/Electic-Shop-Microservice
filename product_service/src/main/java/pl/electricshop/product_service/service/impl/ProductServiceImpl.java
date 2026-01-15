package pl.electricshop.product_service.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import pl.electricshop.product_service.api.dto.ProductDTO;
import pl.electricshop.product_service.api.dto.response.ProductResponse;
import pl.electricshop.product_service.errors.AppError;
import pl.electricshop.product_service.exception.APIException;
import pl.electricshop.product_service.exception.ResourceNotFoundException;
import pl.electricshop.product_service.mapper.ProductMapper;
import pl.electricshop.product_service.model.Category;
import pl.electricshop.product_service.model.Product;
import pl.electricshop.product_service.repository.CategoryRepository;
import pl.electricshop.product_service.repository.ProductRepository;
import pl.electricshop.product_service.service.ProductService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final FileServiceImpl fileService;
    private final ProductMapper productMapper;

    @Value("${project.image}")
    private String imagePath;

    @Value("${image.base.url}")
    private String imageBaseUrl;

    @Value("${project.default}")
    private String defaultImage;


    @Override
    public ProductResponse getAllProducts(Integer pageNumber, Integer pageSize, String sortBy,
                                          String sortOrder, String keyword, String category) {
        Pageable pageDetails = getPageDetails(pageNumber, pageSize, sortBy, sortOrder);

        Specification<Product> spec = buildProductSpecification(keyword, category);
        Page<Product> pageProducts = productRepository.findAll(spec, pageDetails);

        return mapToProductResponse(pageProducts);
    }

    @Override
    public ProductResponse searchByCategory(UUID categoryId, Integer pageNumber,
                                            Integer pageSize, String sortBy, String sortOrder) {
        return null;
    }

    @Override
    public ProductResponse searchProductByKeyWord(String keyword, Integer pageNumber,
                                                  Integer pageSize, String sortBy, String sortOrder) {
        return null;
    }

    @Override
    public ProductDTO addProduct(String categoryNumber, ProductDTO productDTO) {
        Category category = fetchCategoryById(categoryNumber);

        if (productRepository.existsByProductNameIgnoreCase(productDTO.getProductName())) {
            throw new APIException(AppError.ERROR_PRODUCT_EXISTS);
        }

        Product product = productMapper.toEntity(productDTO);

        if (productDTO.getImage() == null || productDTO.getImage().isEmpty()) {
            product.setImage(defaultImage);
        } else {
            product.setImage(productDTO.getImage());
        }

        product.getCategories().add((Category) category);

        product.setSpecialPrice(calculateSpecialPrice(product.getPrice(), product.getDiscount()));

        Product savedProduct = productRepository.save(product);
        ProductDTO savedProductDTO = productMapper.toDTO(savedProduct);
        savedProductDTO.setImage(constructImageUrl(savedProduct.getImage()));
        return savedProductDTO;
    }

    @Override
    public ProductDTO updateProduct(Long productId, ProductDTO productDTO) {
        return null;
    }

    @Override
    public ProductDTO updateProductImage(Long productId, MultipartFile image) {
        return null;
    }

    @Override
    public ProductDTO deleteProductById(Long productId) {
        return null;
    }

    private  Pageable getPageDetails(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {
        if (pageNumber < 0 || pageSize <= 0) {
            throw new IllegalArgumentException("Page number must be non-negative and page size must be greater than 0");
        }

        // Ustawienie domyślnego pola do sortowania, jeśli nie podano
        if (sortBy == null || sortBy.isEmpty()) {
            sortBy = "price";
        }

        // Ustawienie domyślnego kierunku sortowania, jeśli nie podano lub jest niepoprawny
        if (sortOrder == null || (!sortOrder.equalsIgnoreCase("asc")
                && !sortOrder.equalsIgnoreCase("desc"))) {
            sortOrder = "asc"; // Domyślnie sortujemy rosnąco
        }

        Sort sortByAndOrder = Sort.by(Sort.Direction.fromString(sortOrder), sortBy);
        return PageRequest.of(pageNumber, pageSize, sortByAndOrder);
    }

    private Specification<Product> buildProductSpecification(String keyword, String category) {
        Specification<Product> spec = Specification.where(null);
        if (keyword != null && !keyword.isEmpty()) {
            spec = spec.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("productName")),
                            "%" + keyword.toLowerCase() + "%"));
        }

        if (category != null && !category.isEmpty()) {
            spec = spec.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.like(root.get("category").get("categoryName"), category));
        }
        return spec;
    }

    // Helper methods
    private ProductResponse mapToProductResponse(Page<Product> productPage) {
        if (productPage.isEmpty()) throw new APIException(AppError.ERROR_NO_PRODUCTS);

        List<ProductDTO> productDTOS = productPage.getContent().stream()
                .map(product -> {
                    ProductDTO productDTO = productMapper.toDTO(product);
                    productDTO.setImage(constructImageUrl(product.getImage()));
                    return productDTO;
                })
                .toList();
        return new ProductResponse(
                productDTOS,
                productPage.getNumber(),
                productPage.getSize(),
                productPage.getTotalElements(),
                productPage.getTotalPages(),
                productPage.isLast());
    }

    private String constructImageUrl(String imageName) {
        return imageBaseUrl.endsWith("/") ? imageBaseUrl + imageName : imageBaseUrl + "/" + imageName;
    }

    private Category fetchCategoryById(String categoryNumber) {
        return categoryRepository.findByCategoryNumber(categoryNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "categoryNumber", categoryNumber));
    }

    private BigDecimal calculateSpecialPrice(BigDecimal price, BigDecimal discount) {
        // Sprawdzenie czy wartości nie są nullami
        if (price == null || discount == null) {
            return BigDecimal.ZERO;
        }

        // Porównanie: discount.compareTo(BigDecimal.ZERO) < 0 oznacza discount < 0
        if (discount.compareTo(BigDecimal.ZERO) < 0 || discount.compareTo(new BigDecimal("100")) > 0) {
            throw new IllegalArgumentException("Discount must be between 0 and 100");
        }

        // Obliczenie: price * (1 - discount / 100)
        BigDecimal discountRate = discount.divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP);
        BigDecimal discountAmount = price.multiply(discountRate);
        BigDecimal finalPrice = price.subtract(discountAmount);

        // Math.max(0, finalPrice)
        return finalPrice.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : finalPrice;
    }
}
