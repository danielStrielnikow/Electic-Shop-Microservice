package pl.electricshop.cart_service.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

/**
 * Request body dla checkout.
 * Email jest pobierany z headera X-User-Email (przekazywany przez API Gateway z JWT).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CheckoutRequest {

    @NotNull(message = "Address ID is required")
    private UUID addressId;
}
