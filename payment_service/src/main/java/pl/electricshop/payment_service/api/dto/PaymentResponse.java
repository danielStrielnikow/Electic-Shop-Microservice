package pl.electricshop.payment_service.api.dto;

import java.math.BigDecimal;

public record PaymentResponse(
        String clientSecret,     // To najważniejsze dla Stripe Elements
        String stripePublicKey,  // Klucz publiczny (pk_test_...)
        BigDecimal amount,       // Kwota do wyświetlenia userowi
        String currency,         // Waluta
        String status            // Status płatności (PENDING, SUCCEEDED)
) {}
