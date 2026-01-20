package pl.electricshop.user_service.grpc;

import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import pl.electricshop.user_service.exception.EmailNotVerifiedException;
import pl.electricshop.user_service.exception.RefreshTokenExpiredException;
import pl.electricshop.user_service.exception.UserAlreadyExistException;
import pl.electricshop.user_service.filter.Jwt;
import pl.electricshop.common.grpc.auth.*;
import pl.electricshop.user_service.model.User;
import pl.electricshop.user_service.repository.UserRepository;
import pl.electricshop.user_service.service.JwtService;
import pl.electricshop.user_service.service.JwtTokenService;
import pl.electricshop.user_service.service.TokenService;
import pl.electricshop.user_service.validator.PasswordValidator;
import pl.electricshop.user_service.mapper.UserMapper;
import pl.electricshop.user_service.service.EmailVerificationService;

/**
 * gRPC implementation of AuthService.
 * Provides fast, binary protocol communication for authentication operations.
 * Used primarily by API Gateway for login/register/refresh operations.
 */
@GrpcService
@RequiredArgsConstructor
@Slf4j
public class AuthGrpcService extends AuthServiceGrpc.AuthServiceImplBase {

    private final JwtService jwtService;
    private final JwtTokenService jwtTokenService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final PasswordValidator passwordValidator;
    private final UserMapper userMapper;
    private final EmailVerificationService emailVerificationService;
    private final TokenService tokenService;

    @Override
    public void login(LoginRequest request, StreamObserver<AuthResponse> responseObserver) {
        log.debug("gRPC Login request for email: {}", request.getEmail());

        try {
            // Find user by email
            User user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new UsernameNotFoundException("Invalid email or password"));

            // Validate password
            if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                throw new UsernameNotFoundException("Invalid email or password");
            }

            // Check if email is verified
            if (!user.getEmailVerified()) {
                throw new EmailNotVerifiedException("Email not verified");
            }

            // Generate tokens
            String accessToken = jwtService.generateAccessToken(user);
            String refreshToken = jwtService.generateRefreshToken(user);

            // Build response
            AuthResponse response = AuthResponse.newBuilder()
                    .setSuccess(true)
                    .setMessage("Login successful")
                    .setAccessToken(accessToken)
                    .setRefreshToken(refreshToken)
                    .setUser(buildUserInfo(user))
                    .build();

            log.info("gRPC Login successful for user: {}", user.getEmail());
            responseObserver.onNext(response);
            responseObserver.onCompleted();

        } catch (UsernameNotFoundException e) {
            log.debug("gRPC Login failed - invalid credentials");
            responseObserver.onNext(AuthResponse.newBuilder()
                    .setSuccess(false)
                    .setMessage("Invalid email or password")
                    .build());
            responseObserver.onCompleted();

        } catch (EmailNotVerifiedException e) {
            log.debug("gRPC Login failed - email not verified");
            responseObserver.onNext(AuthResponse.newBuilder()
                    .setSuccess(false)
                    .setMessage("Email not verified. Please verify your email before logging in.")
                    .build());
            responseObserver.onCompleted();

        } catch (Exception e) {
            log.error("gRPC Login error", e);
            responseObserver.onNext(AuthResponse.newBuilder()
                    .setSuccess(false)
                    .setMessage("Internal server error")
                    .build());
            responseObserver.onCompleted();
        }
    }

    @Override
    public void register(RegisterRequest request, StreamObserver<AuthResponse> responseObserver) {
        log.debug("gRPC Register request for email: {}", request.getEmail());

        try {
            // Check if user already exists
            if (userRepository.findByEmail(request.getEmail()).isPresent()) {
                throw new UserAlreadyExistException("User with this email already exists");
            }

            // Validate password strength
            passwordValidator.validatePasswordStrength(request.getPassword());

            // Create user
            pl.electricshop.user_service.api.request.RegisterRequest registerRequest =
                    new pl.electricshop.user_service.api.request.RegisterRequest();
            registerRequest.setEmail(request.getEmail());
            registerRequest.setPassword(request.getPassword());

            User user = userMapper.toEntity(registerRequest);
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            userRepository.save(user);

            // Send verification email
            emailVerificationService.generateAndSendVerificationToken(user);

            AuthResponse response = AuthResponse.newBuilder()
                    .setSuccess(true)
                    .setMessage("Registration successful. Please verify your email.")
                    .build();

            log.info("gRPC Registration successful for: {}", request.getEmail());
            responseObserver.onNext(response);
            responseObserver.onCompleted();

        } catch (UserAlreadyExistException e) {
            log.debug("gRPC Register failed - user already exists");
            responseObserver.onNext(AuthResponse.newBuilder()
                    .setSuccess(false)
                    .setMessage("User with this email already exists")
                    .build());
            responseObserver.onCompleted();

        } catch (Exception e) {
            log.error("gRPC Register error", e);
            responseObserver.onNext(AuthResponse.newBuilder()
                    .setSuccess(false)
                    .setMessage("Registration failed: " + e.getMessage())
                    .build());
            responseObserver.onCompleted();
        }
    }

    @Override
    public void refreshToken(RefreshTokenRequest request, StreamObserver<AuthResponse> responseObserver) {
        log.debug("gRPC RefreshToken request");

        try {
            Jwt refreshToken = jwtService.parse(request.getRefreshToken());

            if (refreshToken == null || refreshToken.isExpired()) {
                throw new RefreshTokenExpiredException("Refresh token expired");
            }

            User user = userRepository.findById(refreshToken.getUserId())
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));

            // Generate new tokens
            String newAccessToken = jwtService.generateAccessToken(user);
            String newRefreshToken = jwtService.generateRefreshToken(user);

            AuthResponse response = AuthResponse.newBuilder()
                    .setSuccess(true)
                    .setMessage("Token refreshed successfully")
                    .setAccessToken(newAccessToken)
                    .setRefreshToken(newRefreshToken)
                    .setUser(buildUserInfo(user))
                    .build();

            log.debug("gRPC RefreshToken successful for user: {}", user.getUuid());
            responseObserver.onNext(response);
            responseObserver.onCompleted();

        } catch (RefreshTokenExpiredException e) {
            log.debug("gRPC RefreshToken failed - token expired");
            responseObserver.onNext(AuthResponse.newBuilder()
                    .setSuccess(false)
                    .setMessage("Refresh token expired. Please login again.")
                    .build());
            responseObserver.onCompleted();

        } catch (Exception e) {
            log.error("gRPC RefreshToken error", e);
            responseObserver.onNext(AuthResponse.newBuilder()
                    .setSuccess(false)
                    .setMessage("Token refresh failed")
                    .build());
            responseObserver.onCompleted();
        }
    }

    @Override
    public void validateToken(ValidateTokenRequest request, StreamObserver<ValidateTokenResponse> responseObserver) {
        log.debug("gRPC ValidateToken request");

        try {
            Jwt jwt = jwtService.parse(request.getAccessToken());

            if (jwt == null || jwt.isExpired()) {
                responseObserver.onNext(ValidateTokenResponse.newBuilder()
                        .setValid(false)
                        .setMessage("Token is invalid or expired")
                        .build());
                responseObserver.onCompleted();
                return;
            }

            User user = userRepository.findById(jwt.getUserId())
                    .orElse(null);

            ValidateTokenResponse.Builder responseBuilder = ValidateTokenResponse.newBuilder()
                    .setValid(true)
                    .setMessage("Token is valid");

            if (user != null) {
                responseBuilder.setUser(buildUserInfo(user));
            }

            responseObserver.onNext(responseBuilder.build());
            responseObserver.onCompleted();

        } catch (Exception e) {
            log.debug("gRPC ValidateToken failed: {}", e.getMessage());
            responseObserver.onNext(ValidateTokenResponse.newBuilder()
                    .setValid(false)
                    .setMessage("Token validation failed")
                    .build());
            responseObserver.onCompleted();
        }
    }

    @Override
    public void logout(LogoutRequest request, StreamObserver<LogoutResponse> responseObserver) {
        log.debug("gRPC Logout request for user: {}", request.getUserId());

        try {
            // Invalidate refresh token (add to blacklist in Redis)
            tokenService.invalidateRefreshToken(request.getRefreshToken());

            LogoutResponse response = LogoutResponse.newBuilder()
                    .setSuccess(true)
                    .setMessage("Logged out successfully")
                    .build();

            log.info("gRPC Logout successful for user: {}", request.getUserId());
            responseObserver.onNext(response);
            responseObserver.onCompleted();

        } catch (Exception e) {
            log.error("gRPC Logout error", e);
            responseObserver.onNext(LogoutResponse.newBuilder()
                    .setSuccess(false)
                    .setMessage("Logout failed")
                    .build());
            responseObserver.onCompleted();
        }
    }

    private UserInfo buildUserInfo(User user) {
        return UserInfo.newBuilder()
                .setUserId(user.getUuid().toString())
                .setEmail(user.getEmail())
                .setRole(user.getUserRole().name())
                .build();
    }
}
