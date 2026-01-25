package pl.electricshop.order_service.api;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AddressDTO {
    private Long addressId;
    private String street;
    private String buildingName;
    private String state;
    private String city;
    private String zipCode;
    private String country;
    private String pinCode;
}
