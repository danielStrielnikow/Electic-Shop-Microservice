package pl.electricshop.user_service.filter;

import io.jsonwebtoken.Claims;
import lombok.AllArgsConstructor;
import lombok.Data;
import pl.electricshop.user_service.model.enums.Role;

import java.security.PublicKey; // ZMIANA: Importujemy PublicKey zamiast SecretKey
import java.util.Date;
import java.util.UUID;

@Data
@AllArgsConstructor
public class Jwt {
    private final Claims claims;
    // ZMIANA: Przechowujemy klucz publiczny (służył do weryfikacji przy tworzeniu tego obiektu)
    private final PublicKey key;

    public UUID getUserId(){
        return UUID.fromString(claims.getSubject());
    }

    @Override
    public String toString() {
        // ZMIANA: Przy RSA nie możemy zrobić .signWith(key), ponieważ 'key' to Klucz Publiczny.
        // Kluczem publicznym nie da się podpisać tokena (do tego służy PrivateKey).
        // Dlatego zwracamy po prostu tekstową reprezentację danych (Claims).
        return claims != null ? claims.toString() : "Empty JWT";
    }

    public boolean isExpired() {
        if (claims == null) {
            return false;
        }
        // Sprawdzenie czy data wygaśnięcia nie jest "po" teraz (czyli czy jest w przyszłości)
        // Logika: !after(now) oznacza -> jest przed lub teraz (wygasł)
        return !claims.getExpiration().after(new Date());
    }

    public Role getUserType() {
        String role = claims.get("role", String.class);
        // Zabezpieczenie na wypadek null (opcjonalne, ale zalecane)
        if (role == null) {
            throw new RuntimeException("Token nie zawiera roli użytkownika");
        }
        return Role.valueOf(role);
    }
}