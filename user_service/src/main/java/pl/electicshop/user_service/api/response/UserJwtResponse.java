package pl.electicshop.user_service.api.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class UserJwtResponse {
    private String token;
}
