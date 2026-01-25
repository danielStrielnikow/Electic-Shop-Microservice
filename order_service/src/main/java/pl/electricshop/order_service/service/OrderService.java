package pl.electricshop.order_service.service;


public interface OrderService {

    void proccessOrder(CartCheckoutEvent event);

}
