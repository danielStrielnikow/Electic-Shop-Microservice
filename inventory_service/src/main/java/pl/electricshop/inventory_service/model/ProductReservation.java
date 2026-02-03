package pl.electricshop.inventory_service.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

/**
 * Tymczasowa rezerwacja produktu przechowywana w Redis.
 * TTL = 900 sekund (15 minut)
 */
@RedisHash(value = "reservations", timeToLive = 900)
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ProductReservation {

    @Id
    private String id;  // klucz w Redis: "{userId}:{productNumber}"

    private String productNumber;
    private Integer quantity;
    private String userId;
}
