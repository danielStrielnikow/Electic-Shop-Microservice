package pl.electricshop.payment_service.api;

import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.PaymentIntent;
import com.stripe.net.ApiResource; // <--- WAŻNY IMPORT
import com.stripe.net.Webhook;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;
import pl.electricshop.common.events.payment.PaymentSucceededEvent;
import pl.electricshop.payment_service.model.Payment;
import pl.electricshop.payment_service.repository.PaymentRepository;

import java.time.Instant;

@RestController
@RequestMapping("/api/payments/webhook")
@RequiredArgsConstructor
@Slf4j
public class StripeWebhookController {

    @Value("${stripe.web-hook}")
    private String endpointSecret;

    private final PaymentRepository paymentRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @PostMapping
    @Transactional
    public ResponseEntity<String> handleStripeWebhook(@RequestBody String payload, @RequestHeader("Stripe-Signature") String sigHeader) {
        Event event;

        try {
            event = Webhook.constructEvent(payload, sigHeader, endpointSecret);
        } catch (SignatureVerificationException e) {
            log.error("Nieprawidłowy podpis Stripe!");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid signature");
        }

        if ("payment_intent.succeeded".equals(event.getType())) {

            EventDataObjectDeserializer dataDeserializer = event.getDataObjectDeserializer();
            PaymentIntent intent;

            // PRÓBA 1: Oficjalna deserializacja
            if (dataDeserializer.getObject().isPresent()) {
                intent = (PaymentIntent) dataDeserializer.getObject().get();
            } else {
                // PRÓBA 2: "Koło ratunkowe" - wymuszenie parsowania mimo różnicy wersji
                log.warn("Wersja API Stripe CLI jest nowsza niż SDK. Próbuję parsować surowy JSON...");
                String rawJson = dataDeserializer.getRawJson();
                intent = ApiResource.GSON.fromJson(rawJson, PaymentIntent.class);
            }

            // Dalej już standardowa logika...
            String stripeId = intent.getId();
            log.info("💰 Stripe potwierdził płatność: {}", stripeId);

            Payment payment = paymentRepository.findByPgPaymentId(stripeId)
                    .orElseThrow(() -> {
                        log.error("Nie znaleziono płatności w bazie: {}", stripeId);
                        return new RuntimeException("Payment not found");
                    });

            if ("SUCCEEDED".equals(payment.getPgStatus())) {
                return ResponseEntity.ok("Already Processed");
            }

            payment.setPgStatus("SUCCEEDED");
            paymentRepository.save(payment);

            PaymentSucceededEvent successEvent = new PaymentSucceededEvent(
                    payment.getOrderId().toString(),
                    payment.getPgPaymentId(),
                    payment.getUserId(),
                    payment.getAmount(),
                    payment.getCurrency(),
                    Instant.now().toString()
            );

            kafkaTemplate.send("payment-succeeded-topic", successEvent);
            log.info("SUKCES: Zamówienie {} opłacone.", payment.getOrderId());
        }

        return ResponseEntity.ok("Received");
    }
}