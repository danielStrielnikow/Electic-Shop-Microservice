package pl.electricshop.user_service.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.AllArgsConstructor;
import lombok.Data;
import pl.electricshop.user_service.model.enums.Role;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.UUID;

@Data
@AllArgsConstructor
public class Jwt {
    private final Claims claims;
    private final SecretKey key;

    public UUID getUserId(){
        return UUID.fromString(claims.getSubject());
    }

    @Override
    public String toString() {
        return Jwts.builder()
                .claims(claims)
                .signWith(key)
                .compact();
    }

    public boolean isExpired() {
        if (claims == null) {
            return false;
        }

        return !claims.getExpiration().after(new Date());
    }

    public Role getUserType() {
        String role = claims.get("role", String.class);
        return Role.valueOf(role);
    }

}
