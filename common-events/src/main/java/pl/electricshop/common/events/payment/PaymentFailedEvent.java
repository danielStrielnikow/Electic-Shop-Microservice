package pl.electricshop.common.events.payment;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Zdarzenie informujące o nieudanej próbie płatności.
 * Pozwala na wyzwolenie transakcji kompensacyjnych (zwolnienie towaru).
 */
public record PaymentFailedEvent(
        UUID orderId,           // Powiązane zamówienie
        UUID userId,            // ID użytkownika dla logów i powiadomień
        BigDecimal amount,      // Kwota, której nie udało się pobrać
        String errorCode,       // Kod błędu (np. "INSUFFICIENT_FUNDS", "CARD_EXPIRED")
        String errorMessage,    // Czytelny opis błędu dla systemów monitoringu
        Instant timestamp       // Moment wystąpienia błędu
) {}
