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


    // access token
    public String generateAccessToken(User user) {
        return generateToken(user, jwtConfig.getExpiration());
    }

    // refresh token in 7 days
    public String generateRefreshToken(User user) {
        return generateToken(user, jwtConfig.getRefreshExpiration());
    }

    private String generateToken(User user, int tokenExpirationTime) {
        return Jwts.builder()
                .subject(String.valueOf(user.getUuid()))
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + tokenExpirationTime))
                .signWith(jwtConfig.getSecretKey())
                .claim("email", user.getEmail())
                .claim("role", user.getUserRole().name())
                .compact();
    }

    public Jwt parse(String token) {
        var claims = Jwts.parser().verifyWith(jwtConfig.getSecretKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return new Jwt(claims, jwtConfig.getSecretKey());
    }
}
