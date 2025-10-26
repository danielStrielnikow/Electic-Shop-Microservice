package pl.electicshop.user_service.api.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class EmailVerificationStatusResponse {
    private boolean verified;
    private String email;
    private String message;

    public static EmailVerificationStatusResponse verified(String email) {
        return new EmailVerificationStatusResponse(true, email, "Email is verified");
    }

    public static EmailVerificationStatusResponse notVerified(String email) {
        return new EmailVerificationStatusResponse(false, email, "Email is not verified");
    }

    public static EmailVerificationStatusResponse userNotFound() {
        return new EmailVerificationStatusResponse(false, null, "User not found");
    }
}
