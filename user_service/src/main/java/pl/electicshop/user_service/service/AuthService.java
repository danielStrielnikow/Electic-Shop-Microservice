package pl.electicshop.user_service.service;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import pl.electicshop.user_service.api.request.LoginRequest;
import pl.electicshop.user_service.api.request.RegisterRequest;
import pl.electicshop.user_service.api.response.RegisterResponse;
import pl.electicshop.user_service.api.response.UserJwtResponse;
import pl.electicshop.user_service.exception.EmailNotVerifiedException;
import pl.electicshop.user_service.exception.UserAlreadyExistException;
import pl.electicshop.user_service.mapper.UserMapper;
import pl.electicshop.user_service.model.User;
import pl.electicshop.user_service.repository.UserRepository;
import pl.electicshop.user_service.validator.PasswordValidator;

/**
 * Authentication Service (SRP - handles only authentication logic)
 * Uses DRY principle by delegating token management to JwtTokenService
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {
    private final JwtService jwtService;
    private final JwtTokenService jwtTokenService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final PasswordValidator passwordValidator;
    private final UserMapper userMapper;
    private final EmailVerificationService emailVerificationService;

    /**
     * Registers a new user with password hashing
     */
    @Transactional
    public RegisterResponse registerUser(RegisterRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new UserAlreadyExistException("User with this email already exist");
        }

        passwordValidator.validatePasswordStrength(request.getPassword());

        User user = userMapper.toEntity(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        userRepository.save(user);

        emailVerificationService.generateAndSendVerificationToken(user);

//        log.info("User registered successfully: {}", user.getEmail());

        return RegisterResponse.success(user.getEmail());
    }

    /**
     * Authenticates user and returns access token with refresh token cookie
     */
    public UserJwtResponse loginUser(@Valid LoginRequest request, HttpServletResponse response) {
        // Authenticate user
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        if (!user.getEmailVerified()) {
            throw new EmailNotVerifiedException(
                    "Email not verified. Please check your email and verify your account before logging in."
            );
        }

        // Generate access token (FIX: was using generateRefreshToken)
        String accessToken = jwtService.generateAccessToken(user);

        // Generate refresh token cookie (DRY: delegated to JwtTokenService)
        HttpCookie refreshCookie = jwtTokenService.generateRefreshTokenCookie(user);
        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());

        log.info("User logged in successfully: {}", user.getEmail());

        return new UserJwtResponse(accessToken);
    }
}
