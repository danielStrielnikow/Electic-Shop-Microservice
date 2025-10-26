package pl.electicshop.user_service.validator;

import org.springframework.stereotype.Component;
import pl.electicshop.user_service.exception.PasswordResetException;

import java.util.regex.Pattern;

@Component
public class PasswordValidator {

    private static final Pattern UPPERCASE_PATTERN = Pattern.compile(".*[A-Z].*");
    private static final Pattern LOWERCASE_PATTERN = Pattern.compile(".*[a-z].*");
    private static final Pattern DIGIT_PATTERN = Pattern.compile(".*[0-9].*");
    private static final Pattern SPECIAL_CHAR_PATTERN = Pattern.compile(".*[!@#$%^&*()_+\\-=\\[\\]{};':\",./<>?].*");

    private static final int MIN_LENGTH = 8;
    private static final int MAX_LENGTH = 48;

    public void validatePasswordStrength(String password) {
        if (password == null || password.length() < MIN_LENGTH) {
            throw new PasswordResetException("Password must be at least " + MIN_LENGTH + " characters long");
        }

        if (password.length() > MAX_LENGTH) {
            throw new PasswordResetException("Password cannot exceed " + MAX_LENGTH + " characters");
        }

        if (!UPPERCASE_PATTERN.matcher(password).matches()) {
            throw new PasswordResetException("Password must contain at least one uppercase letter");
        }

        if (!LOWERCASE_PATTERN.matcher(password).matches()) {
            throw new PasswordResetException("Password must contain at least one lowercase letter");
        }

        if (!DIGIT_PATTERN.matcher(password).matches()) {
            throw new PasswordResetException("Password must contain at least one digit");
        }

        if (!SPECIAL_CHAR_PATTERN.matcher(password).matches()) {
            throw new PasswordResetException("Password must contain at least one special character");
        }
    }

    public boolean isPasswordValid(String password) {
        try {
            validatePasswordStrength(password);
            return true;
        } catch (PasswordResetException e) {
            return false;
        }
    }
}
