package pl.electricshop.user_service.api.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pl.electricshop.user_service.model.enums.Role;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * DTO for returning user data to clients
 * Does NOT include sensitive information like password
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

    private UUID uuid;

    private String email;

    private Role userRole;

    private Boolean emailVerified;

    @Builder.Default
    private List<AddressResponse> addresses = new ArrayList<>();

}
