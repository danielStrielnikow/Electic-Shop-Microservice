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

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentEventListener {

    private final PaymentRepository paymentRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @KafkaListener(topics = "order-created-topic", groupId = "payment-service-group")
    @Transactional
    public void handleOrderCreated(OrderCreatedEvent event) {
        log.info("Odebrano nowe zamówienie: {}", event.getOrderId());

        // 1. Stwórz PaymentIntent w Stripe
        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setAmount(event.getAmountToPay().multiply(new BigDecimal(100)).longValue()) // Stripe chce kwotę w groszach (cents)!
                .setCurrency("pln")
                .setAutomaticPaymentMethods(
                        PaymentIntentCreateParams.AutomaticPaymentMethods.builder().setEnabled(true).build()
                )
                .putMetadata("orderId", event.getOrderId().toString()) // Ważne: powiązanie ID zamówienia w Stripe
                .build();

        try {
            PaymentIntent intent = PaymentIntent.create(params);

            // 2. Zapisz płatność w Twojej bazie Postgres
            Payment payment = new Payment();
            payment.setOrderId(event.getOrderId());
            payment.setAmount(event.getAmountToPay());
            payment.setCurrency(event.getCurrency());
            payment.setPgPaymentId(intent.getId()); // ID ze Stripe (pi_123xyz...)
            payment.setClientSecret(intent.getClientSecret()); // KLUCZOWE dla Frontendu!

            paymentRepository.save(payment);

            log.info("Zainicjowano płatność Stripe: {}", intent.getId());

            // 3. Wyślij event PaymentSucceededEvent
            paymentSucceeded(new PaymentSucceededEvent(
                    payment.getOrderId().toString(),
                    payment.getPgPaymentId(),
                    "3afff0cb-cd89-4977-842b-0e1d3491f504", // userId - brak w tym evencie
                    payment.getAmount(),
                    payment.getCurrency(),
                    Instant.now().toString()
            ));
        } catch (StripeException e) {
            log.error("Błąd Stripe", e);
            // Tu można wysłać event PaymentFailedEvent
        }
    }


    private void paymentSucceeded(PaymentSucceededEvent event) {
        kafkaTemplate.send("payment-succeeded-topic", event);
    }

    private void paymentFailed(PaymentFailedEvent event) {
        kafkaTemplate.send("payment-failed-topic", event);
    }
}
