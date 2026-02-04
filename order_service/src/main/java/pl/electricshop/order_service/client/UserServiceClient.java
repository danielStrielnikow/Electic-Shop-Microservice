package pl.electricshop.order_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import pl.electricshop.order_service.api.AddressDTO;

import java.util.UUID;

@FeignClient(name = "user-service", path = "/api/addresses")
public interface UserServiceClient {

    @GetMapping("/internal/{addressId}")
    AddressDTO getAddressById(@PathVariable("addressId") UUID addressId);

}
