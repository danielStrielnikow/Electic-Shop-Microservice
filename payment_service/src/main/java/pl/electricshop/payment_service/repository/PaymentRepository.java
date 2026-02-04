package pl.electricshop.payment_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.electricshop.payment_service.model.Payment;

import java.util.Optional;
import java.util.UUID;
@Repository
public interface PaymentRepository extends JpaRepository<Payment, UUID> {
    Optional<Payment> findByOrderId(UUID orderId);

    Optional<Payment> findByPgPaymentId(String stripeId);
}
