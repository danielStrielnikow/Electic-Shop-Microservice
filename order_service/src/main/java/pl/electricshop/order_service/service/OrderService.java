package pl.electricshop.order_service.service;


import pl.electricshop.common.events.cart.CartCheckoutEvent;

import java.util.UUID;

public interface OrderService {

    void proccessOrder(CartCheckoutEvent event);

    void finalizeOrder(UUID orderId);
}
