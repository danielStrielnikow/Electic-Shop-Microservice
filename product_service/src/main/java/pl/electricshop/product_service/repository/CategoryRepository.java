package pl.electricshop.product_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.electricshop.product_service.model.Category;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CategoryRepository extends JpaRepository<Category, UUID> {
    Category findByCategoryName(String categoryName);
    Optional<Category> findByCategoryNumber(String categoryNumber);
}
