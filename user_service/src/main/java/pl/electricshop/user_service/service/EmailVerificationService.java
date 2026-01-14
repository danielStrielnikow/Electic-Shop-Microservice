package pl.electricshop.user_service.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pl.electricshop.user_service.api.request.ResendVerificationEmailRequest;
import pl.electricshop.user_service.api.response.EmailVerificationStatusResponse;
import pl.electricshop.user_service.api.response.TokenValidationResponse;
import pl.electricshop.user_service.exception.TokenValidationException;
import pl.electricshop.user_service.model.User;
import pl.electricshop.common.events.UserRegistrationEvent;
import pl.electricshop.user_service.repository.UserRepository;
import pl.electricshop.user_service.model.kafka.EventPublisher;
import pl.electricshop.user_service.validator.TokenValidationResult;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Service for email verification operations
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailVerificationService {
    private final UserRepository userRepository;
    private final TokenService tokenService;
    private final RateLimitService rateLimitService;
    private final EventPublisher eventPublisher;

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

            generateAndSendVerificationToken(user);
            return true;
        }catch (Exception e) {
            log.error("Failed to send verification email to {}", email, e);
            return false;
        }
    }

    public boolean resendVerificationEmail(ResendVerificationEmailRequest request) {
        return sendVerificationEmail(request.getEmail());
    }

    public void generateAndSendVerificationToken(User user) {
        String email = user.getEmail();
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(15); // 15 minutes for email verification
        String token = tokenService.generateEmailVerificationToken(email, expiresAt);

        UserRegistrationEvent event = new UserRegistrationEvent(email, token);

        eventPublisher.publishUserRegistration(event);
    }

    @Transactional
    public TokenValidationResponse verifyEmail(String token) {
        TokenValidationResult result = tokenService.validateEmailVerificationToken(token);

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
