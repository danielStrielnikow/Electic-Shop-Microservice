package pl.electricshop.common.events.payment;

import java.math.BigDecimal;

/**
 * Zdarzenie informujące o nieudanej próbie płatności.
 */
public record PaymentFailedEvent(
        String orderId,         // UUID jako String
        String userId,          // UUID jako String
        BigDecimal amount,      // Kwota
        String errorCode,       // Kod błędu (np. "INSUFFICIENT_FUNDS")
        String errorMessage,    // Czytelny opis błędu
        String timestamp        // ISO timestamp jako String
) {}
