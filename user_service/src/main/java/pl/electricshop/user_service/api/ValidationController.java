package pl.electricshop.user_service.api;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.electricshop.user_service.api.request.ResendVerificationEmailRequest;
import pl.electricshop.user_service.api.response.EmailVerificationStatusResponse;
import pl.electricshop.user_service.api.response.OperationResponse;
import pl.electricshop.user_service.api.response.TokenValidationResponse;
import pl.electricshop.user_service.service.EmailVerificationService;
import pl.electricshop.user_service.service.PasswordService;

@RequiredArgsConstructor
@RestController
@RequestMapping("/auth")
public class ValidationController {
    private final PasswordService passwordService;
    private final EmailVerificationService emailVerificationService;

    @GetMapping("/validate-password-token/{token}")
    public ResponseEntity<TokenValidationResponse> validateToken(@PathVariable String token) {
        TokenValidationResponse response = passwordService.validateResetToken(token);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/resend-verification-email")
    public ResponseEntity<OperationResponse> resendVerificationEmail(@Valid @RequestBody ResendVerificationEmailRequest request) {
        boolean success = emailVerificationService.resendVerificationEmail(request);
        if (success) {
            return ResponseEntity.ok(OperationResponse.success("Verification email sent"));
        } else {
            return ResponseEntity.badRequest().body(OperationResponse.error("Failed to send verification email"));
        }
    }

    @GetMapping("/validate-verification-email-token/{token}")
    public ResponseEntity<TokenValidationResponse> validateVerificationToken(@PathVariable String token) {
        TokenValidationResponse response = emailVerificationService.validateVerificationToken(token);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/verify-email/{token}")
    public ResponseEntity<Boolean> verifyEmail(@PathVariable String token) {
        TokenValidationResponse response = emailVerificationService.verifyEmail(token);

        if (response.isValid()) {
            return ResponseEntity.ok(true);
        }

        if (response.getMessage().contains("already verified")) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(false);
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(false);
    }

    @GetMapping("/email-verification-status")
    public ResponseEntity<EmailVerificationStatusResponse> checkEmailVerificationStatus(@RequestParam String email) {
        EmailVerificationStatusResponse response = emailVerificationService.checkEmailVerificationStatus(email);
        return ResponseEntity.ok(response);
    }
}
