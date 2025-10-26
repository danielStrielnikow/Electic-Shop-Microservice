package pl.electicshop.user_service.service;

import io.jsonwebtoken.ExpiredJwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpCookie;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import pl.electicshop.user_service.api.response.UserJwtResponse;
import pl.electicshop.user_service.config.JwtConfig;
import pl.electicshop.user_service.exception.RefreshTokenExpiredException;
import pl.electicshop.user_service.filter.Jwt;
import pl.electicshop.user_service.model.User;
import pl.electicshop.user_service.repository.UserRepository;

/**
 * Service responsible for JWT token lifecycle management (SRP)
 * - Generating refresh token cookies
 * - Refreshing access tokens
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class JwtTokenService {
    private final JwtService jwtService;
    private final JwtConfig jwtConfig;
    private final UserRepository userRepository;

    /**
     * Generates HTTP-only secure cookie with refresh token
     */
    public HttpCookie generateRefreshTokenCookie(User user) {
        String refreshToken = jwtService.generateRefreshToken(user);

        return ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)
                .secure(true)
                .path("/auth")
                .maxAge(jwtConfig.getRefreshExpiration())
                .sameSite("None")
                .build();
    }

    /**
     * Refreshes access token using refresh token
     */
    public UserJwtResponse refreshAccessToken(String refreshTokenString) throws ExpiredJwtException {
        Jwt refreshToken = jwtService.parse(refreshTokenString);

        if (refreshToken == null || refreshToken.isExpired()) {
            throw new RefreshTokenExpiredException("Refresh token expired");
        }

        User user = userRepository.findById(refreshToken.getUserId())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        String newAccessToken = jwtService.generateAccessToken(user);
        return new UserJwtResponse(newAccessToken);
    }
}
