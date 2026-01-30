package pl.electricshop.cart_service.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Koszyk użytkownika przechowywany w Redis.
 * TTL: 259200 sekund = 3 dni
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@RedisHash(value = "carts", timeToLive = 900)
public class Cart implements Serializable {

    @Id
    private UUID userId;

    private List<CartItem> items = new ArrayList<>();


    private LocalDateTime  reservationUntil;

    /**
     * Oblicza całkowitą cenę koszyka.
     */
    public BigDecimal getTotalPrice() {
        return items.stream()
                .map(item -> {
                    BigDecimal price = BigDecimal.valueOf(item.getProductPrice());
                    BigDecimal qty = BigDecimal.valueOf(item.getQuantity());
                    BigDecimal discount = BigDecimal.valueOf(item.getDiscount() != null ? item.getDiscount() : 0);
                    return price.multiply(qty).subtract(discount);
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public Cart(UUID userId, List<CartItem> items) {
        this.userId = userId;
        this.items = items;
    }
}
