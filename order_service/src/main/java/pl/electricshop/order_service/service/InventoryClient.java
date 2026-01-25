package pl.electricshop.order_service.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pl.electricshop.common.events.cart.CartItemPayload;

import java.util.List;

@Slf4j
@Service // To sprawia, że Spring to widzi i wstrzyknie do OrderService
public class InventoryClient {

    public boolean reserveProducts(List<?> items) { // Użyj właściwego typu listy z Eventu
        log.info("STUB: Symulacja - wywołanie gRPC do Inventory.");
        log.info("STUB: Rezerwacja {} produktów zakończona sukcesem.", items.size());

        // Zawsze zwracamy true, żeby OrderService poszedł dalej
        return true;
    }

    // Metoda do odblokowania (kompensacji) - na przyszłość
    public void releaseProducts(List<?> items) {
        log.info("STUB: Symulacja - zwalnianie rezerwacji.");
    }
}
