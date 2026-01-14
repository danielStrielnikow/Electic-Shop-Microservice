package pl.electricshop.user_service.api.response;

public class TokenValidationResult {
    private final boolean valid;
    private final String email;
    private final String errorMessage;

    private TokenValidationResult(boolean valid, String email, String errorMessage) {
        this.valid = valid;
        this.email = email;
        this.errorMessage = errorMessage;
    }

    public static TokenValidationResult valid(String email) {
        return new TokenValidationResult(true, email, null);
    }

    public static TokenValidationResult invalid(String errorMessage) {
        return new TokenValidationResult(false, null, errorMessage);
    }

    public boolean isValid() {
        return valid;
    }

    public String getEmail() {
        return email;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
