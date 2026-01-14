package pl.electricshop.user_service.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.electricshop.user_service.api.request.ChangePasswordRequest;
import pl.electricshop.user_service.api.request.ForgotPasswordRequest;
import pl.electricshop.user_service.api.request.ResetPasswordRequest;
import pl.electricshop.user_service.api.response.TokenValidationResponse;
import pl.electricshop.user_service.exception.PasswordResetException;
import pl.electricshop.user_service.exception.TokenValidationException;
import pl.electricshop.user_service.exception.TooManyRequestsException;
import pl.electricshop.user_service.model.User;
import pl.electricshop.common.events.PasswordResetEvent;
import pl.electricshop.user_service.repository.UserRepository;
import pl.electricshop.user_service.model.kafka.EventPublisher;
import pl.electricshop.user_service.validator.PasswordValidator;
import pl.electricshop.user_service.validator.TokenValidationResult;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Service for password operations (reset, change)
 * Uses self-injection (@Lazy) to enable @Async proxy for internal method calls
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PasswordService {
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final PasswordValidator passwordValidator;
    private final TokenService tokenService;
    private final RateLimitService rateLimitService;
    private final EventPublisher eventPublisher;





    public void requestPasswordReset(ForgotPasswordRequest request) {
        if (rateLimitService.isRateLimitedPassword(request.getEmail())) {
            throw new TooManyRequestsException("Too many password reset requests. Please wait before trying again.");
        }

        Optional<User> userOpt = userRepository.findByEmail(request.getEmail());
        boolean emailExists = userOpt.isPresent();

        if (emailExists) {
            generateAndSendResetToken(request.getEmail());
        }
    }

    private void generateAndSendResetToken(String email) {
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(5);
        String token = tokenService.generatePasswordResetToken(email, expiresAt);

        PasswordResetEvent event = new PasswordResetEvent(email, token);

        eventPublisher.publishPasswordReset(event);
    }


    public TokenValidationResponse validateResetToken(String token) {
        TokenValidationResult result = tokenService.validatePasswordResetToken(token);

        if (result.isValid()) {
            return TokenValidationResponse.valid();
        } else {
            throw new TokenValidationException("Password reset token is invalid.");
        }
    }


    @Transactional
    public boolean resetPassword(ResetPasswordRequest request) {
        try {
            TokenValidationResult result = tokenService.validatePasswordResetToken(request.getToken());

            if (!result.isValid()) {
                return false;
            }

            User user = userRepository.findByEmail(result.getEmail()).orElse(null);
            if (user == null) {
                return false;
            }

            passwordValidator.validatePasswordStrength(request.getNewPassword());

            if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
                return false;
            }

            user.setPassword(passwordEncoder.encode(request.getNewPassword()));
            userRepository.save(user);

            return true;
        } catch (Exception e) {
            log.error("Password reset failed", e);
            return false;
        }
    }


    @Transactional
    public void changePassword(User user, ChangePasswordRequest request) {
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new PasswordResetException("Current password is incorrect");
        }

        passwordValidator.validatePasswordStrength(request.getNewPassword());

        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw new PasswordResetException("New password must be different from current password");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

}
