package pl.electicshop.user_service.api;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.electicshop.user_service.api.request.LoginRequest;
import pl.electicshop.user_service.api.request.RegisterRequest;
import pl.electicshop.user_service.api.response.OperationResponse;
import pl.electicshop.user_service.api.response.RegisterResponse;
import pl.electicshop.user_service.api.response.UserJwtResponse;
import pl.electicshop.user_service.service.AuthService;
import pl.electicshop.user_service.service.JwtTokenService;

/**
 * Authentication Controller (SRP - handles only HTTP layer)
 * Business logic delegated to AuthService (DRY)
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;
    private final JwtTokenService jwtTokenService;

    @PostMapping("/login")
    public ResponseEntity<UserJwtResponse> login(@Valid @RequestBody LoginRequest request, HttpServletResponse response) {
        UserJwtResponse token = authService.loginUser(request, response);
        return ResponseEntity.ok(token);
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
        RegisterResponse response = authService.registerUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/refresh")
    public ResponseEntity<UserJwtResponse> refreshToken(@CookieValue("refreshToken") String refreshToken) {
        UserJwtResponse token = jwtTokenService.refreshAccessToken(refreshToken);
        return ResponseEntity.ok(token);
    }

    @PostMapping("/logout")
    public ResponseEntity<OperationResponse> logout(HttpServletResponse response) {
        Cookie clearCookie = new Cookie("refreshToken", "");
        clearCookie.setMaxAge(0);
        clearCookie.setPath("/auth");
        response.addCookie(clearCookie);

        return ResponseEntity.ok(OperationResponse.success("Logout successful"));
    }
}
