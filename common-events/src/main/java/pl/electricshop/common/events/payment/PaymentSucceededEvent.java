package pl.electricshop.common.events.payment;

import java.math.BigDecimal;

/**
 * Zdarzenie informujące o poprawnym opłaceniu zamówienia.
 */
public record PaymentSucceededEvent(
        String orderId,         // UUID jako String
        String paymentId,       // Identyfikator z systemu płatności (np. Stripe)
        String userId,          // ID użytkownika
        BigDecimal amount,      // Kwota
        String currency,        // Waluta (np. "PLN")
        String timestamp        // ISO timestamp jako String
) {}
