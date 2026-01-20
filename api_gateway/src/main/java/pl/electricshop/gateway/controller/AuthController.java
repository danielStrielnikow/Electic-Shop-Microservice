package pl.electricshop.gateway.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.bind.annotation.*;
import pl.electricshop.gateway.dto.AuthResponseDto;
import pl.electricshop.gateway.dto.LoginRequestDto;
import pl.electricshop.gateway.dto.RegisterRequestDto;
import pl.electricshop.gateway.grpc.AuthGrpcClient;
import pl.electricshop.common.grpc.auth.AuthResponse;
import reactor.core.publisher.Mono;

/**
 * Authentication Controller - handles auth requests via gRPC to user_service.
 *
 * Flow:
 * 1. Client sends HTTP request to Gateway
 * 2. Gateway calls user_service via gRPC (fast, binary protocol)
 * 3. Gateway returns HTTP response with tokens
 *
 * Why gRPC for auth:
 * - Login/Register are called frequently
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthGrpcClient authGrpcClient;

    private static final int REFRESH_TOKEN_MAX_AGE = 7 * 24 * 60 * 60; // 7 days

    @PostMapping("/login")
    public Mono<ResponseEntity<AuthResponseDto>> login(
            @RequestBody LoginRequestDto request,
            ServerHttpResponse response) {

        log.info("Login request for email: {}", request.getEmail());

        return authGrpcClient.login(request.getEmail(), request.getPassword())
                .map(grpcResponse -> {
                    if (grpcResponse.getSuccess()) {
                        // Set refresh token as HTTP-only cookie
                        if (grpcResponse.hasRefreshToken()) {
                            setRefreshTokenCookie(response, grpcResponse.getRefreshToken());
                        }

                        return ResponseEntity.ok(mapToDto(grpcResponse));
                    } else {
                        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                .body(AuthResponseDto.error(grpcResponse.getMessage()));
                    }
                })
                .onErrorResume(e -> {
                    log.error("Login error", e);
                    return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                            .body(AuthResponseDto.error("Authentication service unavailable")));
                });
    }

    @PostMapping("/register")
    public Mono<ResponseEntity<AuthResponseDto>> register(@RequestBody RegisterRequestDto request) {
        log.info("Register request for email: {}", request.getEmail());

        return authGrpcClient.register(request.getEmail(), request.getPassword())
                .map(grpcResponse -> {
                    if (grpcResponse.getSuccess()) {
                        return ResponseEntity.status(HttpStatus.CREATED)
                                .body(mapToDto(grpcResponse));
                    } else {
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                .body(AuthResponseDto.error(grpcResponse.getMessage()));
                    }
                })
                .onErrorResume(e -> {
                    log.error("Register error", e);
                    return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                            .body(AuthResponseDto.error("Authentication service unavailable")));
                });
    }

    @PostMapping("/refresh")
    public Mono<ResponseEntity<AuthResponseDto>> refreshToken(
            @CookieValue(name = "refreshToken", required = false) String refreshTokenCookie,
            @RequestHeader(value = "X-Refresh-Token", required = false) String refreshTokenHeader,
            ServerHttpResponse response) {

        // Try cookie first, then header
        String refreshToken = refreshTokenCookie != null ? refreshTokenCookie : refreshTokenHeader;

        if (refreshToken == null || refreshToken.isBlank()) {
            return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(AuthResponseDto.error("Refresh token is required")));
        }

        log.debug("Refresh token request");

        return authGrpcClient.refreshToken(refreshToken)
                .map(grpcResponse -> {
                    if (grpcResponse.getSuccess()) {
                        // Update refresh token cookie
                        if (grpcResponse.hasRefreshToken()) {
                            setRefreshTokenCookie(response, grpcResponse.getRefreshToken());
                        }

                        return ResponseEntity.ok(mapToDto(grpcResponse));
                    } else {
                        // Clear cookie on failure
                        clearRefreshTokenCookie(response);
                        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                .body(AuthResponseDto.error(grpcResponse.getMessage()));
                    }
                })
                .onErrorResume(e -> {
                    log.error("Refresh token error", e);
                    return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                            .body(AuthResponseDto.error("Authentication service unavailable")));
                });
    }

    @PostMapping("/logout")
    public Mono<ResponseEntity<AuthResponseDto>> logout(
            @CookieValue(name = "refreshToken", required = false) String refreshToken,
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            ServerHttpResponse response) {

        // Clear cookie regardless of gRPC result
        clearRefreshTokenCookie(response);

        if (refreshToken == null || userId == null) {
            return Mono.just(ResponseEntity.ok(
                    AuthResponseDto.builder()
                            .success(true)
                            .message("Logged out")
                            .build()));
        }

        return authGrpcClient.logout(refreshToken, userId)
                .map(grpcResponse -> ResponseEntity.ok(
                        AuthResponseDto.builder()
                                .success(grpcResponse.getSuccess())
                                .message(grpcResponse.getMessage())
                                .build()))
                .onErrorResume(e -> {
                    log.error("Logout error (token may already be invalid)", e);
                    // Still return success - user is logged out from Gateway perspective
                    return Mono.just(ResponseEntity.ok(
                            AuthResponseDto.builder()
                                    .success(true)
                                    .message("Logged out")
                                    .build()));
                });
    }

    private void setRefreshTokenCookie(ServerHttpResponse response, String refreshToken) {
        ResponseCookie cookie = ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)
                .secure(true)
                .path("/api/v1/auth")
                .maxAge(REFRESH_TOKEN_MAX_AGE)
                .sameSite("Strict")
                .build();

        response.addCookie(cookie);
    }

    private void clearRefreshTokenCookie(ServerHttpResponse response) {
        ResponseCookie cookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(true)
                .path("/api/v1/auth")
                .maxAge(0)
                .sameSite("Strict")
                .build();

        response.addCookie(cookie);
    }

    private AuthResponseDto mapToDto(AuthResponse grpcResponse) {
        AuthResponseDto.AuthResponseDtoBuilder builder = AuthResponseDto.builder()
                .success(grpcResponse.getSuccess())
                .message(grpcResponse.getMessage());

        if (grpcResponse.hasAccessToken()) {
            builder.accessToken(grpcResponse.getAccessToken());
        }

        // Note: refreshToken is set as cookie, not in response body for security
        // But we can include it for mobile clients that don't use cookies
        if (grpcResponse.hasRefreshToken()) {
            builder.refreshToken(grpcResponse.getRefreshToken());
        }

        if (grpcResponse.hasUser()) {
            builder.user(AuthResponseDto.UserInfoDto.builder()
                    .userId(grpcResponse.getUser().getUserId())
                    .email(grpcResponse.getUser().getEmail())
                    .role(grpcResponse.getUser().getRole())
                    .build());
        }

        return builder.build();
    }
}
