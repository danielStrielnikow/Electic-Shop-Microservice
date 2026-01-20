package pl.electricshop.common.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when there's not enough stock for a product.
 */
public class InsufficientStockException extends BusinessException {

    public InsufficientStockException(String productNumber, int requested, int available) {
        super(
                String.format("Insufficient stock for product %s. Requested: %d, Available: %d",
                        productNumber, requested, available),
                HttpStatus.CONFLICT,
                "INSUFFICIENT_STOCK"
        );
    }

    public InsufficientStockException(String message) {
        super(message, HttpStatus.CONFLICT, "INSUFFICIENT_STOCK");
    }
}
