package pl.electricshop.inventory_service.model.api;

import java.util.UUID;

public record ProductReservationDTO(
        String productNumber,
        Integer quantity,
        UUID userId) {
}
