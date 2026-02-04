package pl.electricshop.common.events.product;

public record ProductEvent(
        String productNumber,
        int quantity) {
}
