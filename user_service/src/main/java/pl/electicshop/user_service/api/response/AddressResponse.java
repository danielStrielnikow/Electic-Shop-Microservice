package pl.electicshop.user_service.api.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pl.electicshop.user_service.model.enums.Country;

import java.util.UUID;

/**
 * DTO for returning address data to clients
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddressResponse {

    private UUID uuid;

    private String street;

    private String buildingName;

    private String city;

    private String state;

    private Country country;

    private String pinCode;

    private UUID userId;
}