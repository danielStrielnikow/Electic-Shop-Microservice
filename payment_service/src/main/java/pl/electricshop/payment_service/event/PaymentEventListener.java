package pl.electricshop.payment_service.event;

import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import pl.electricshop.common.events.payment.OrderCreatedEvent;
import pl.electricshop.common.events.payment.PaymentFailedEvent;
import pl.electricshop.common.events.payment.PaymentSucceededEvent;
import pl.electricshop.payment_service.model.Payment;
import pl.electricshop.payment_service.repository.PaymentRepository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentEventListener {

    private final PaymentRepository paymentRepository;

    @KafkaListener(topics = "order-created-topic", groupId = "payment-service-group")
    @Transactional
    public void handleOrderCreated(OrderCreatedEvent event) {
        log.info("Odebrano nowe zamówienie: {}", event.getOrderId());

        String productSummary = event.getItems().stream()
                .map(item -> item.getProductName() + " (x" + item.getQuantity() + ")")
                .collect(Collectors.joining(", "));


        if (productSummary.length() > 490) {
            productSummary = productSummary.substring(0, 490) + "...";
        }

        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setAmount(event.getAmountToPay().multiply(new BigDecimal(100)).longValue())
                .setCurrency("pln")
                .setAutomaticPaymentMethods(
                        PaymentIntentCreateParams.AutomaticPaymentMethods.builder().setEnabled(true).build()
                )
                .putMetadata("orderId", event.getOrderId().toString())
                .putMetadata("product_summary", productSummary)
                .putMetadata("email", event.getEmail())
                .build();

        try {
            PaymentIntent intent = PaymentIntent.create(params);

            Payment payment = new Payment();
            payment.setOrderId(event.getOrderId());
            payment.setUserId(event.getUserId());
            payment.setAmount(event.getAmountToPay());
            payment.setCurrency(event.getCurrency());
            payment.setPaymentMethod(event.getPaymentMethod());
            payment.setPgPaymentId(intent.getId());
            payment.setClientSecret(intent.getClientSecret());
            payment.setPgStatus("PENDING");
            payment.setPgName("STRIPE");

            paymentRepository.save(payment);

            log.info("Zainicjowano płatność Stripe: {}. Czekam na ruch użytkownika.", intent.getId());


        } catch (StripeException e) {
            log.error("Błąd Stripe przy tworzeniu Intent", e);
        }
    }
}
