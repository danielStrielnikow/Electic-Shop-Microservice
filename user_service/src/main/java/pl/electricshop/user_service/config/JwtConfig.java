package pl.electricshop.user_service.config;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Slf4j
@Configuration
@ConfigurationProperties(prefix = "app.jwt")
@Data
public class JwtConfig {

    // Tutaj wpadnie string "classpath:certs/private.pem" z YAML-a
    private String privateKey;
    private String publicKey;
    private int expiration;
    private int refreshExpiration;

    public RSAPrivateKey getRsaPrivateKey() {
        try {
            // 1. Wczytaj treść pliku zamiast samej ścieżki
            String keyContent = loadKeyContent(this.privateKey);

            // 2. Wyczyść nagłówki
            String cleanKey = cleanKey(keyContent);

            // 3. Dekoduj Base64
            byte[] encoded = Base64.getDecoder().decode(cleanKey);

            // 4. Generuj klucz (PKCS#8 dla Private Key)
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return (RSAPrivateKey) keyFactory.generatePrivate(new PKCS8EncodedKeySpec(encoded));
        } catch (Exception e) {
            log.error("Błąd wczytywania klucza prywatnego. Ścieżka: {}", privateKey, e);
            throw new RuntimeException("Nie udało się wczytać klucza prywatnego", e);
        }
    }

    public RSAPublicKey getRsaPublicKey() {
        try {
            // 1. Wczytaj treść pliku
            String keyContent = loadKeyContent(this.publicKey);

            // 2. Wyczyść nagłówki
            String cleanKey = cleanKey(keyContent);

            // 3. Dekoduj Base64
            byte[] encoded = Base64.getDecoder().decode(cleanKey);

            // 4. Generuj klucz (X509 dla Public Key)
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return (RSAPublicKey) keyFactory.generatePublic(new X509EncodedKeySpec(encoded));
        } catch (Exception e) {
            log.error("Błąd wczytywania klucza publicznego. Ścieżka: {}", publicKey, e);
            throw new RuntimeException("Nie udało się wczytać klucza publicznego", e);
        }
    }

    // Metoda pomocnicza: Sprawdza czy to classpath, czy raw string
    private String loadKeyContent(String keyConfig) throws IOException {
        if (keyConfig.startsWith("classpath:")) {
            String path = keyConfig.replace("classpath:", "");
            return new String(StreamUtils.copyToByteArray(new ClassPathResource(path).getInputStream()), StandardCharsets.UTF_8);
        }
        // Jeśli ktoś wkleił klucz bezpośrednio do YAML
        return keyConfig;
    }

    private String cleanKey(String key) {
        return key
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replace("-----BEGIN RSA PRIVATE KEY-----", "") // Na wszelki wypadek
                .replace("-----END RSA PRIVATE KEY-----", "")
                .replaceAll("\\s+", ""); // Usuwa spacje, entery, tabulatory
    }
}