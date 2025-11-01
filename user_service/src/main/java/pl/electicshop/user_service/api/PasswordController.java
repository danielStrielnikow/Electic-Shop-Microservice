package pl.electicshop.user_service.api;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.electicshop.user_service.api.request.ChangePasswordRequest;
import pl.electicshop.user_service.api.request.ForgotPasswordRequest;
import pl.electicshop.user_service.api.request.ResetPasswordRequest;
import pl.electicshop.user_service.api.response.OperationResponse;
import pl.electicshop.user_service.model.User;
import pl.electicshop.user_service.service.PasswordService;
import pl.electicshop.user_service.service.UserService;

import java.util.UUID;

/**
 * Password Controller - Password management operations
 * Business logic delegated to PasswordService
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/auth")
public class PasswordController {
    private final PasswordService passwordService;
    private final UserService userService;


    @PostMapping("/change-password")
    public ResponseEntity<OperationResponse> changePassword(@Valid @RequestBody ChangePasswordRequest request,
                                                            Authentication authentication) {
        UUID userId = (UUID) authentication.getPrincipal();
        User user = userService.getUserEntityById(userId);

        passwordService.changePassword(user, request);
        return ResponseEntity.ok(OperationResponse.success("Password changed successfully"));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<OperationResponse> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        passwordService.requestPasswordReset(request);
        return ResponseEntity.ok(OperationResponse.success("Password reset email sent"));
    }

    @PostMapping("/reset-password")
    public  ResponseEntity<OperationResponse> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        boolean success = passwordService.resetPassword(request);

        if (success) {
            return ResponseEntity.ok(OperationResponse.success("Password reset successfully"));
        } else {
            return ResponseEntity.badRequest().body(OperationResponse.error("Password reset failed"));
        }
    }


}
