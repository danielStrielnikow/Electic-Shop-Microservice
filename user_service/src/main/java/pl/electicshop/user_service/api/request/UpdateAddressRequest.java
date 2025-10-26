package pl.electicshop.user_service.api.request;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import pl.electicshop.user_service.model.enums.Country;

/**
 * DTO for updating an existing address
 * All fields are optional to support partial updates
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateAddressRequest {

    @Size(min = 5, max = 255, message = "Street name must be between 5 and 255 characters")
    private String street;

    @Size(min = 5, max = 255, message = "Building name must be between 5 and 255 characters")
    private String buildingName;

    @Size(min = 4, max = 100, message = "City name must be between 4 and 100 characters")
    private String city;

    @Size(min = 2, max = 100, message = "State name must be between 2 and 100 characters")
    private String state;

    private Country country;

    @Size(min = 5, max = 20, message = "Pin code must be between 5 and 20 characters")
    private String pinCode;
}