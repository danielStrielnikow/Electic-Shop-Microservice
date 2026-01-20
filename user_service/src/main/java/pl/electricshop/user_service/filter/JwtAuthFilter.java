package pl.electricshop.user_service.filter;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import pl.electricshop.user_service.service.JwtService;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

/**
 * JWT Authentication Filter with Gateway header support.
 * This filter supports two modes:
 * 1. Gateway mode: Reads user info from X-User-Id, X-User-Role headers (set by API Gateway)
 * 2. Direct mode: Parses JWT token from Authorization header (fallback for development/testing)
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    // Headers set by API Gateway after JWT validation
    public static final String USER_ID_HEADER = "X-User-Id";
    public static final String USER_EMAIL_HEADER = "X-User-Email";
    public static final String USER_ROLE_HEADER = "X-User-Role";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // First, check for Gateway headers (preferred path)
        String userId = request.getHeader(USER_ID_HEADER);
        String userRole = request.getHeader(USER_ROLE_HEADER);

        if (userId != null && userRole != null) {
            // Request came through Gateway - trust the headers
            log.debug("Processing request with Gateway headers for user: {}", userId);
            authenticateFromGatewayHeaders(request, userId, userRole);
            filterChain.doFilter(request, response);
            return;
        }

        // Fallback: Direct access - parse JWT token
        var authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        var tokenString = authHeader.replace("Bearer ", "");

        try {
            Jwt token = jwtService.parse(tokenString);
            if (token == null || token.isExpired()) {
                filterChain.doFilter(request, response);
                return;
            }

            List<GrantedAuthority> authorities = List.of(
                    new SimpleGrantedAuthority("ROLE_" + token.getUserType().name())
            );

            var authentication = new UsernamePasswordAuthenticationToken(
                    token.getUserId(),  // UUID as principal
                    null,
                    authorities
            );

            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);

            log.debug("JWT validated directly for user: {}", token.getUserId());

        } catch (JwtException e) {
            // Invalid JWT token - log and continue without authentication
            log.debug("Invalid JWT token: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Authenticate user based on headers propagated by API Gateway.
     * Gateway has already validated the JWT, so we trust these headers.
     */
    private void authenticateFromGatewayHeaders(HttpServletRequest request, String userId, String userRole) {
        try {
            UUID uuid = UUID.fromString(userId);
            List<GrantedAuthority> authorities = List.of(
                    new SimpleGrantedAuthority("ROLE_" + userRole)
            );

            var authentication = new UsernamePasswordAuthenticationToken(
                    uuid,
                    null,
                    authorities
            );
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);

            log.debug("Authenticated via Gateway headers for user: {}", userId);

        } catch (IllegalArgumentException e) {
            log.warn("Invalid user ID in Gateway header: {}", userId);
        }
    }
}
