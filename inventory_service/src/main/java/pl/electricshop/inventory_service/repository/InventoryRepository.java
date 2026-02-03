package pl.electricshop.inventory_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pl.electricshop.inventory_service.model.Inventory;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, UUID> {
    Optional<Inventory> findByProductNumber(String productNumber);

    @Modifying
    @Query("UPDATE Inventory i SET " +
            "i.reservedQuantity = i.reservedQuantity - :amount, " +
            "i.availableQuantity = i.availableQuantity + :amount " +
            "WHERE i.productNumber = :sku AND i.reservedQuantity >= :amount")
    void releaseStock(String sku, int amount);

    /**
     * Znajduje produkty z niezerową ilością zarezerwowaną.
     * Używane do cleanup wygasłych rezerwacji.
     */
    @Query("SELECT i FROM Inventory i WHERE i.reservedQuantity > 0")
    List<Inventory> findAllWithReservations();
}
