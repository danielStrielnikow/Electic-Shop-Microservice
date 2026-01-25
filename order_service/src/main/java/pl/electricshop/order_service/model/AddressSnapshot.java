package pl.electricshop.order_service.model;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddressSnapshot {
    private String street;
    private String buildingName;
    private String city;
    private String state;
    private String zipCode;
    private String country;
    private Long originalAddressId;
}
