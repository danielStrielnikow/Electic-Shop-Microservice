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
import pl.electricshop.payment_service.model.Payment;
import pl.electricshop.payment_service.repository.PaymentRepository;

import java.math.BigDecimal;

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
            paymentSucceeded();
        } catch (StripeException e) {
            log.error("Błąd Stripe", e);
            paymentFailed();
            // Tu można wysłać event PaymentFailedEvent
        }
    }


    private void paymentSucceeded() {
        kafkaTemplate.send("payment-succeeded-topic", "Przykładowa wiadomość o sukcesie płatności");
    }

    private void paymentFailed() {
        kafkaTemplate.send("payment-failed-topic", "Przykładowa wiadomość o niepowodzeniu płatności");
    }
}
