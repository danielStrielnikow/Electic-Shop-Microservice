package pl.electicshop.user_service.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import pl.electicshop.user_service.validator.TokenValidationResult;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

@Service
@Slf4j
public class TokenService {
    private static final String HMAC_ALGORITHM = "HmacSHA256";
    private static final String SEPARATOR = "|";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    @Value("${app.security.hmac-secret:default-secret-change-in-production}")
    private String hmacSecret;

    /**
     * Generates a password reset token for the given email
     */
    public String generatePasswordResetToken(String email, LocalDateTime expiresAt) {
        log.debug("Generate password reset token for email: {}", email);
        return generateToken("", email, expiresAt);
    }

    /**
     * Validates a password reset token
     */
    public TokenValidationResult validatePasswordResetToken(String token) {
        return validateToken(token, "", "Token validation failed");
    }

    /**
     * Generates an email verification token for the given email
     */
    public String generateEmailVerificationToken(String email, LocalDateTime expiresAt) {
        log.debug("Generated email verification token for email: {}", email);
        return generateToken("verify:", email, expiresAt);
    }

    /**
     * Validates an email verification token
     */
    public TokenValidationResult validateEmailVerificationToken(String token) {
        return validateToken(token, "verify:", "Email verification token validation failed");
    }

    /**
     * Generic method to generate a token with optional prefix
     */
    private String generateToken(String prefix, String email, LocalDateTime expiresAt) {
        String data = prefix + email + SEPARATOR + expiresAt.format(FORMATTER);
        String signature = generateHmac(data);

        return Base64.getUrlEncoder().withoutPadding()
                .encodeToString((data + SEPARATOR + signature).getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Generic method to validate a token with expected prefix
     */
    private TokenValidationResult validateToken(String token, String expectedPrefix, String logMessage) {
        try {
            String decoded = new String(Base64.getUrlDecoder().decode(token), StandardCharsets.UTF_8);
            String[] parts = decoded.split("\\" + SEPARATOR);

            if (parts.length != 3) {
                return TokenValidationResult.invalid("Invalid token format");
            }

            String emailWithPrefix = parts[0];
            String expiresAtStr = parts[1];
            String providedSignature = parts[2];

            // Validate prefix
            if (!emailWithPrefix.startsWith(expectedPrefix)) {
                return TokenValidationResult.invalid("Invalid token type");
            }

            // Extract email
            String email = emailWithPrefix.substring(expectedPrefix.length());

            // Verify signature
            String data = emailWithPrefix + SEPARATOR + expiresAtStr;
            String expectedSignature = generateHmac(data);

            if (!expectedSignature.equals(providedSignature)) {
                return TokenValidationResult.invalid("Invalid token signature");
            }

            // Check expiration
            LocalDateTime expiresAt = LocalDateTime.parse(expiresAtStr, FORMATTER);
            if (LocalDateTime.now().isAfter(expiresAt)) {
                return TokenValidationResult.invalid("Token has expired");
            }

            return TokenValidationResult.valid(email);

        } catch (Exception e) {
            log.warn("{}: {}", logMessage, e.getMessage());
            return TokenValidationResult.invalid("Invalid token");
        }
    }

    /**
     * Generates HMAC signature for the given data
     */
    private String generateHmac(String data) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            SecretKeySpec secretKeySpec = new SecretKeySpec(hmacSecret.getBytes(StandardCharsets.UTF_8),
                    HMAC_ALGORITHM);

            mac.init(secretKeySpec);
            byte[] hmacBytes = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));

            return Base64.getUrlEncoder().withoutPadding().encodeToString(hmacBytes);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("Failed to generate HMAC ", e);
        }
    }
}
