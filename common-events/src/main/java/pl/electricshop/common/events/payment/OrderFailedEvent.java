package pl.electricshop.common.events.payment;

import java.util.List;
import java.util.UUID;

/**
 * Zdarzenie wysyłane, gdy zamówienie nie może zostać sfinalizowane.
 * Służy do wyzwalania transakcji kompensacyjnych (np. zwolnienie towaru).
 */
public record OrderFailedEvent(
        UUID orderId,                // ID zamówienia, które zawiodło
        List<OrderItemPayload> items, // Lista przedmiotów do zwrócenia na stan
        String reason,               // Powód awarii (np. "PAYMENT_REJECTED", "INVENTORY_TIMEOUT")
        String email                 // Opcjonalnie do powiadomienia klienta
) {}
