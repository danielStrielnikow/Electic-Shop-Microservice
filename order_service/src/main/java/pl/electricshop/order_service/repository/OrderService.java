package pl.electricshop.order_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.electricshop.order_service.model.Order;

import java.util.UUID;

@Repository
public interface OrderService extends JpaRepository<Order, UUID> {
}
