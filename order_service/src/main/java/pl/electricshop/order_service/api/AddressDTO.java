package pl.electricshop.order_service.api;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AddressDTO {
    private UUID addressId;
    private String street;
    private String buildingName;
    private String state;
    private String city;
    private String zipCode;
    private String country;
    private String pinCode;
    private UUID userId;
}
