package pl.electricshop.product_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import pl.electricshop.product_service.model.Product;

import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID> , JpaSpecificationExecutor<Product> {
//    Page<Product> findByCategoryOrderByPriceAsc(Category category, Pageable pageDetails);
//    Page<Product> findByProductNameLikeIgnoreCase(String keyword, Pageable pageDetails);

    boolean existsByProductNameIgnoreCase(String productName);
}
