package pl.electricshop.common.events.payment;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Zdarzenie informujące o poprawnym opłaceniu zamówienia.
 * Wykorzystujemy 'record' z Javy 21 dla niemutowalności i czystości kodu.
 */
public record PaymentSucceededEvent(
        UUID orderId,           // Unikalny identyfikator zamówienia
        String paymentId,       // Identyfikator z systemu płatności (np. z PayU/Stripe)
        UUID userId,            // ID użytkownika (opcjonalnie, pomocne przy logach)
        BigDecimal amount,      // Kwota, która została faktycznie pobrana
        String currency,        // Waluta (np. "PLN")
        Instant timestamp       // Moment zaksięgowania płatności
) {}
