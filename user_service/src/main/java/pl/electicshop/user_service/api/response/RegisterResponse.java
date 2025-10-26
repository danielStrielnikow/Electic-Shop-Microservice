package pl.electicshop.user_service.api.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RegisterResponse {
    private String message;
    private String email;

    public static RegisterResponse success(String email) {
        return new RegisterResponse(
                "Registration successful. Please check your email to verify your account.",
                email
        );
    }
}
