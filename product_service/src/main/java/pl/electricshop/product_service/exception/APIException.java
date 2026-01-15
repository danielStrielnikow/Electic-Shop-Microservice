package pl.electricshop.product_service.exception;

public class APIException extends RuntimeException{
    private static final long serialVersion = 1L;

    public APIException() {
    }

    public APIException(String message) {
        super(message);
    }
}
