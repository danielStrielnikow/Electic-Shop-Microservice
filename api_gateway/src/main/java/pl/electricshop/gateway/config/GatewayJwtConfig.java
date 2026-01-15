package pl.electricshop.gateway.config;

import io.jsonwebtoken.security.Keys;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import javax.crypto.SecretKey;
import java.util.List;

/**
 * Configuration properties for JWT validation and Gateway routing.
 */
@Configuration
@ConfigurationProperties(prefix = "app")
@Data
public class GatewayJwtConfig {

    private JwtProperties jwt = new JwtProperties();
    private GatewayProperties gateway = new GatewayProperties();

    @Data
    public static class JwtProperties {
        private String secret;

        public SecretKey getSecretKey() {
            return Keys.hmacShaKeyFor(secret.getBytes());
        }
    }

    @Data
    public static class GatewayProperties {
        private List<String> publicPaths;
    }
}
