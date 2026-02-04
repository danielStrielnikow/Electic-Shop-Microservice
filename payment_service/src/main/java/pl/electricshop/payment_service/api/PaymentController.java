package pl.electricshop.payment_service.api;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.electricshop.payment_service.api.dto.PaymentResponse;
import pl.electricshop.payment_service.model.Payment;
import pl.electricshop.payment_service.repository.PaymentRepository;

import java.util.UUID;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentRepository paymentRepository;

    @Value("${stripe.publishable-key}")
    private String stripePublicKey;

    @GetMapping("/order/{orderId}")
    public ResponseEntity<PaymentResponse> getPaymentDetails(@PathVariable UUID orderId) {
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        PaymentResponse response = new PaymentResponse(
                payment.getClientSecret(),
                stripePublicKey,
                payment.getAmount(),
                payment.getCurrency(),
                payment.getPgStatus()
        );

        return ResponseEntity.ok(response);
    }
}
