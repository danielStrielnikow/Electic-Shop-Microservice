package pl.electricshop.order_service.service;


import pl.electricshop.common.events.cart.CartCheckoutEvent;

public interface OrderService {

    void proccessOrder(CartCheckoutEvent event);
}
