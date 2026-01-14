package pl.electricshop.user_service.api.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import pl.electricshop.user_service.model.enums.Country;

import java.util.UUID;

/**
 * DTO for creating a new address
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddressRequest {

    @NotBlank(message = "Street is required")
    @Size(min = 5, max = 255, message = "Street name must be between 5 and 255 characters")
    private String street;

    @NotBlank(message = "Building name is required")
    @Size(min = 5, max = 255, message = "Building name must be between 5 and 255 characters")
    private String buildingName;

    @NotBlank(message = "City is required")
    @Size(min = 4, max = 100, message = "City name must be between 4 and 100 characters")
    private String city;

    @NotBlank(message = "State is required")
    @Size(min = 2, max = 100, message = "State name must be between 2 and 100 characters")
    private String state;

    @NotNull(message = "Country is required")
    private Country country;

    @NotBlank(message = "Pin code is required")
    @Size(min = 5, max = 20, message = "Pin code must be between 5 and 20 characters")
    private String pinCode;

    @NotNull(message = "User ID is required")
    private UUID userId;
}
