package pl.electricshop.gateway.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import pl.electricshop.gateway.config.GatewayJwtConfig;
import reactor.core.publisher.Mono;

import java.util.Date;
import java.util.List;

/**
 * Global JWT Authentication Filter for Spring Cloud Gateway.
 * <p>
 * This filter:
 * 1. Checks if the request path is public (no auth needed)
 * 2. Validates the JWT token from Authorization header
 * 3. Propagates user info via custom headers to downstream services
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    private final GatewayJwtConfig config;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    // Custom headers to propagate to downstream services
    public static final String USER_ID_HEADER = "X-User-Id";
    public static final String USER_EMAIL_HEADER = "X-User-Email";
    public static final String USER_ROLE_HEADER = "X-User-Role";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        // Check if path is public
        if (isPublicPath(path)) {
            log.debug("Public path accessed: {}", path);
            return chain.filter(exchange);
        }

        // Get Authorization header
        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.debug("Missing or invalid Authorization header for path: {}", path);
            return onError(exchange, "Missing or invalid Authorization header", HttpStatus.UNAUTHORIZED);
        }

        String token = authHeader.substring(7); // Remove "Bearer " prefix

        try {
            // Parse and validate JWT with RS256 public key
            Claims claims = Jwts.parser()
                    .verifyWith(config.getJwt().getRsaPublicKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            // Check expiration
            if (claims.getExpiration().before(new Date())) {
                log.debug("Token expired for path: {}", path);
                return onError(exchange, "Token expired", HttpStatus.UNAUTHORIZED);
            }

            // Extract user information
            String userId = claims.getSubject();
            String email = claims.get("email", String.class);
            String role = claims.get("role", String.class);

            // Check admin access
            if (path.startsWith("/api/admin") && !"ADMIN".equals(role)) {
                log.debug("Non-admin user attempting to access admin path: {}", path);
                return onError(exchange, "Access denied - Admin role required", HttpStatus.FORBIDDEN);
            }

            // Add user info to headers for downstream services
            ServerHttpRequest mutatedRequest = request.mutate()
                    .header(USER_ID_HEADER, userId)
                    .header(USER_EMAIL_HEADER, email != null ? email : "")
                    .header(USER_ROLE_HEADER, role != null ? role : "")
                    .build();

            log.debug("JWT validated successfully for user: {} on path: {}", userId, path);

            return chain.filter(exchange.mutate().request(mutatedRequest).build());

        } catch (ExpiredJwtException e) {
            log.debug("Expired JWT token: {}", e.getMessage());
            return onError(exchange, "Token expired", HttpStatus.UNAUTHORIZED);
        } catch (JwtException e) {
            log.debug("Invalid JWT token: {}", e.getMessage());
            return onError(exchange, "Invalid token", HttpStatus.UNAUTHORIZED);
        }
    }

    private boolean isPublicPath(String path) {
        List<String> publicPaths = config.getGateway().getPublicPaths();
        if (publicPaths == null) {
            return false;
        }
        return publicPaths.stream().anyMatch(pattern -> pathMatcher.match(pattern, path));
    }

    private Mono<Void> onError(ServerWebExchange exchange, String message, HttpStatus status) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        response.getHeaders().add("Content-Type", "application/json");

        String body = String.format("{\"error\": \"%s\", \"status\": %d}", message, status.value());

        return response.writeWith(
                Mono.just(response.bufferFactory().wrap(body.getBytes()))
        );
    }

    @Override
    public int getOrder() {
        // Run before other filters
        return -100;
    }
}
