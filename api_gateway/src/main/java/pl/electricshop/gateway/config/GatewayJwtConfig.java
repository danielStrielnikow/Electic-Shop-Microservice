package pl.electricshop.gateway.config;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.List;

/**
 * Configuration properties for JWT validation (RS256) and Gateway routing.
 * Gateway only needs PUBLIC KEY for token verification - never the private key!
 */
@Slf4j
@Configuration
@ConfigurationProperties(prefix = "app")
@Data
public class GatewayJwtConfig {

    private JwtProperties jwt = new JwtProperties();
    private GatewayProperties gateway = new GatewayProperties();

    @Data
    public static class JwtProperties {
        private String publicKey;

        private RSAPublicKey rsaPublicKey;

        public RSAPublicKey getRsaPublicKey() {
            if (rsaPublicKey == null) {
                rsaPublicKey = loadPublicKey();
            }
            return rsaPublicKey;
        }

        private RSAPublicKey loadPublicKey() {
            try {
                String keyContent = loadKeyContent(this.publicKey);
                String cleanKey = cleanKey(keyContent);
                byte[] encoded = Base64.getDecoder().decode(cleanKey);

                KeyFactory keyFactory = KeyFactory.getInstance("RSA");
                return (RSAPublicKey) keyFactory.generatePublic(new X509EncodedKeySpec(encoded));
            } catch (Exception e) {
                log.error("Failed to load RSA public key from: {}", publicKey, e);
                throw new RuntimeException("Cannot load RSA public key", e);
            }
        }

        private String loadKeyContent(String keyConfig) throws IOException {
            if (keyConfig == null) {
                throw new IllegalStateException("JWT public key path is not configured (app.jwt.public-key)");
            }
            if (keyConfig.startsWith("classpath:")) {
                String path = keyConfig.replace("classpath:", "");
                return new String(
                        StreamUtils.copyToByteArray(new ClassPathResource(path).getInputStream()),
                        StandardCharsets.UTF_8
                );
            }
            // Direct key content in YAML
            return keyConfig;
        }

        private String cleanKey(String key) {
            return key
                    .replace("-----BEGIN PUBLIC KEY-----", "")
                    .replace("-----END PUBLIC KEY-----", "")
                    .replaceAll("\\s+", "");
        }
    }

    @Data
    public static class GatewayProperties {
        private List<String> publicPaths;
    }
}
