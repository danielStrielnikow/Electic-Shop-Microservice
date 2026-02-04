package pl.electricshop.payment_service.api.dto;

public record StripePaymentDTO(Long amount, String currency) {
}
