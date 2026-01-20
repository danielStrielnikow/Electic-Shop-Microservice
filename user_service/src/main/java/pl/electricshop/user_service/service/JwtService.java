package pl.electricshop.user_service.service;

import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.electricshop.user_service.config.JwtConfig;
import pl.electricshop.user_service.filter.Jwt;
import pl.electricshop.user_service.model.User;

import java.util.Date;

@Service
@RequiredArgsConstructor
public class JwtService {
    private final JwtConfig jwtConfig;

    // Access token
    public String generateAccessToken(User user) {
        return generateToken(user, jwtConfig.getExpiration());
    }

    // Refresh token
    public String generateRefreshToken(User user) {
        return generateToken(user, jwtConfig.getRefreshExpiration());
    }

    private String generateToken(User user, int tokenExpirationTime) {
        return Jwts.builder()
                .subject(String.valueOf(user.getUuid()))
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + tokenExpirationTime))
                // ZMIANA: Używamy klucza PRYWATNEGO i algorytmu RS256
                .signWith(jwtConfig.getRsaPrivateKey(), Jwts.SIG.RS256)
                .claim("email", user.getEmail())
                .claim("role", user.getUserRole().name())
                .compact();
    }

    public Jwt parse(String token) {
        // ZMIANA: Do weryfikacji używamy klucza PUBLICZNEGO
        var claims = Jwts.parser()
                .verifyWith(jwtConfig.getRsaPublicKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();

        // Tutaj przekazujemy klucz publiczny do obiektu Jwt (jeśli Twoja klasa Jwt tego wymaga)
        // Jeśli klasa Jwt służy tylko do trzymania claims, możesz pominąć drugi argument
        return new Jwt(claims, jwtConfig.getRsaPublicKey());
    }
}