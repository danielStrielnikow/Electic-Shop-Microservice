package pl.electicshop.user_service.service;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import pl.electicshop.user_service.api.request.ResendVerificationEmailRequest;
import pl.electicshop.user_service.api.response.EmailVerificationStatusResponse;
import pl.electicshop.user_service.api.response.TokenValidationResponse;
import pl.electicshop.user_service.exception.TokenValidationException;
import pl.electicshop.user_service.model.User;
import pl.electicshop.user_service.repository.UserRepository;
import pl.electicshop.user_service.validator.TokenValidationResult;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Service for email verification operations
 * Uses self-injection (@Lazy) to enable @Async proxy for internal method calls
 */
@Service
@Slf4j
public class EmailVerificationService {
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final TokenService tokenService;
    private final RateLimitService rateLimitService;

    // Self-injection to enable @Async proxy (fixes self-invocation issue)
    private final EmailVerificationService self;

    public EmailVerificationService(UserRepository userRepository, EmailService emailService,
                                    TokenService tokenService, RateLimitService rateLimitService,
                                    @Lazy EmailVerificationService self){
        this.userRepository = userRepository;
        this.emailService = emailService;
        this.tokenService = tokenService;
        this.rateLimitService = rateLimitService;
        this.self = self;
    }

    public boolean sendVerificationEmail(String email) {
        try {
            Optional<User> userOpt = userRepository.findByEmail(email);
            if (userOpt.isEmpty()) {
                return false;
            }

            User user = userOpt.get();
            if (user.getEmailVerified()) {
                return false;
            }

            if (rateLimitService.isEmailVerificationRateLimited(email)) {
                return false;
            }

            self.generateAndSendVerificationToken(user);
            return true;
        }catch (Exception e) {
            log.error("Failed to send verification email to {}", email, e);
            return false;
        }
    }

    public boolean resendVerificationEmail(ResendVerificationEmailRequest request) {
        return sendVerificationEmail(request.getEmail());
    }

    /**
     * Generate and send email verification token asynchronously
     * Runs in background thread to avoid blocking user registration
     * Must be called through self proxy (self.method()) to enable @Async
     */
    @Async
    protected void generateAndSendVerificationToken(User user) {
        String email = user.getEmail();
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(15); // 15 minutes for email verification
        String token = tokenService.generateEmailVerificationToken(email, expiresAt);

        emailService.sendEmailConfirmation(email, token);
    }

    @Transactional
    public TokenValidationResponse verifyEmail(String token) {
        TokenValidationResult result = tokenService.validatePasswordResetToken(token);

        if (!result.isValid()) {
            return TokenValidationResponse.invalid("Invalid or expired token");
        }

        User user = userRepository.findByEmail(result.getEmail()).orElse(null);


        if (user == null) {
            return TokenValidationResponse.invalid("User not found");
        }

        if (user.getEmailVerified()) {
            return TokenValidationResponse.invalid("Email is already verified");
        }

        user.setEmailVerified(true);
        userRepository.save(user);

        return TokenValidationResponse.valid();
    }

    public TokenValidationResponse validateVerificationToken(String token) {
        TokenValidationResult result = tokenService.validateEmailVerificationToken(token);

        if (result.isValid()) {
            return TokenValidationResponse.valid();
        } else {
            throw new TokenValidationException("email verification token invalid");
        }
    }

    public EmailVerificationStatusResponse checkEmailVerificationStatus(String email) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            return EmailVerificationStatusResponse.userNotFound();
        }

        User user = userOpt.get();
        if (user.getEmailVerified()) {
            return EmailVerificationStatusResponse.verified(email);
        } else {
            return EmailVerificationStatusResponse.notVerified(email);
        }
    }
}
