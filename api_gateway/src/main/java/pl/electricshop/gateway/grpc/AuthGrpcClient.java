package pl.electricshop.gateway.grpc;

import io.grpc.StatusRuntimeException;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;
import pl.electricshop.common.grpc.auth.*;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 * gRPC client for AuthService in user_service.
 * Wraps blocking gRPC calls in reactive Mono for WebFlux compatibility.
 *
 * Benefits of gRPC over REST for auth:
 * - ~10x faster serialization (Protocol Buffers vs JSON)
 * - HTTP/2 multiplexing and header compression
 * - Strong typing and contract enforcement
 * - Persistent connections (no TCP handshake per request)
 */
@Service
@Slf4j
public class AuthGrpcClient {

    @GrpcClient("user-service")
    private AuthServiceGrpc.AuthServiceBlockingStub authServiceStub;

    /**
     * Login user via gRPC
     */
    public Mono<AuthResponse> login(String email, String password) {
        return Mono.fromCallable(() -> {
            log.debug("gRPC login request for: {}", email);

            LoginRequest request = LoginRequest.newBuilder()
                    .setEmail(email)
                    .setPassword(password)
                    .build();

            return authServiceStub.login(request);
        })
        .subscribeOn(Schedulers.boundedElastic())
        .doOnSuccess(response -> log.debug("gRPC login response: success={}", response.getSuccess()))
        .doOnError(e -> log.error("gRPC login error", e));
    }

    /**
     * Register new user via gRPC
     */
    public Mono<AuthResponse> register(String email, String password) {
        return Mono.fromCallable(() -> {
            log.debug("gRPC register request for: {}", email);

            RegisterRequest request = RegisterRequest.newBuilder()
                    .setEmail(email)
                    .setPassword(password)
                    .build();

            return authServiceStub.register(request);
        })
        .subscribeOn(Schedulers.boundedElastic())
        .doOnSuccess(response -> log.debug("gRPC register response: success={}", response.getSuccess()))
        .doOnError(e -> log.error("gRPC register error", e));
    }

    /**
     * Refresh access token via gRPC
     */
    public Mono<AuthResponse> refreshToken(String refreshToken) {
        return Mono.fromCallable(() -> {
            log.debug("gRPC refresh token request");

            RefreshTokenRequest request = RefreshTokenRequest.newBuilder()
                    .setRefreshToken(refreshToken)
                    .build();

            return authServiceStub.refreshToken(request);
        })
        .subscribeOn(Schedulers.boundedElastic())
        .doOnSuccess(response -> log.debug("gRPC refresh token response: success={}", response.getSuccess()))
        .doOnError(e -> log.error("gRPC refresh token error", e));
    }

    /**
     * Validate token via gRPC (for internal service-to-service calls)
     */
    public Mono<ValidateTokenResponse> validateToken(String accessToken) {
        return Mono.fromCallable(() -> {
            log.debug("gRPC validate token request");

            ValidateTokenRequest request = ValidateTokenRequest.newBuilder()
                    .setAccessToken(accessToken)
                    .build();

            return authServiceStub.validateToken(request);
        })
        .subscribeOn(Schedulers.boundedElastic())
        .doOnSuccess(response -> log.debug("gRPC validate token response: valid={}", response.getValid()))
        .doOnError(e -> log.error("gRPC validate token error", e));
    }

    /**
     * Logout user via gRPC (invalidate refresh token)
     */
    public Mono<LogoutResponse> logout(String refreshToken, String userId) {
        return Mono.fromCallable(() -> {
            log.debug("gRPC logout request for user: {}", userId);

            LogoutRequest request = LogoutRequest.newBuilder()
                    .setRefreshToken(refreshToken)
                    .setUserId(userId)
                    .build();

            return authServiceStub.logout(request);
        })
        .subscribeOn(Schedulers.boundedElastic())
        .doOnSuccess(response -> log.debug("gRPC logout response: success={}", response.getSuccess()))
        .doOnError(e -> log.error("gRPC logout error", e));
    }

    /**
     * Check if gRPC service is available
     */
    public Mono<Boolean> isServiceAvailable() {
        return validateToken("test")
                .map(response -> true)
                .onErrorReturn(StatusRuntimeException.class, false);
    }
}
