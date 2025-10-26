package pl.electicshop.user_service.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.electicshop.user_service.api.request.ChangePasswordRequest;
import pl.electicshop.user_service.api.request.ForgotPasswordRequest;
import pl.electicshop.user_service.api.request.ResetPasswordRequest;
import pl.electicshop.user_service.api.response.TokenValidationResponse;
import pl.electicshop.user_service.exception.PasswordResetException;
import pl.electicshop.user_service.exception.TokenValidationException;
import pl.electicshop.user_service.exception.TooManyRequestsException;
import pl.electicshop.user_service.model.User;
import pl.electicshop.user_service.repository.UserRepository;
import pl.electicshop.user_service.validator.PasswordValidator;
import pl.electicshop.user_service.validator.TokenValidationResult;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Service for password operations (reset, change)
 * Uses self-injection (@Lazy) to enable @Async proxy for internal method calls
 */
@Service
@Slf4j
public class PasswordService {
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final PasswordValidator passwordValidator;
    private final TokenService tokenService;
    private final RateLimitService rateLimitService;

    // Self-injection to enable @Async proxy (fixes self-invocation issue)
    private final PasswordService self;

    public PasswordService(PasswordEncoder passwordEncoder, UserRepository userRepository,
                           EmailService emailService, PasswordValidator passwordValidator,
                           TokenService tokenService, RateLimitService rateLimitService, @Lazy PasswordService self) {
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
        this.emailService = emailService;
        this.passwordValidator = passwordValidator;
        this.tokenService = tokenService;
        this.rateLimitService = rateLimitService;
        this.self = self;
    }



    public void requestPasswordReset(ForgotPasswordRequest request) {
        if (rateLimitService.isRateLimitedPassword(request.getEmail())) {
            throw new TooManyRequestsException("Too many password reset requests. Please wait before trying again.");
        }

        Optional<User> userOpt = userRepository.findByEmail(request.getEmail());
        boolean emailExists = userOpt.isPresent();

        if (emailExists) {
            // Call through proxy to enable @Async
            self.generateAndSendResetToken(request.getEmail());
        }
    }

    /**
     * Generate and send password reset token asynchronously
     * Runs in background thread to avoid blocking the password reset request
     * Must be called through self proxy (self.method()) to enable @Async
     */
    @Async
    protected void generateAndSendResetToken(String email) {
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(5);
        String token = tokenService.generatePasswordResetToken(email, expiresAt);


        emailService.sendPasswordResetEmail(email, token);
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
