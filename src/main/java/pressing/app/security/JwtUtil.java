package pressing.app.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;

/**
 * Utilitaire pour la génération et la validation des tokens JWT.
 * Utilisé exclusivement pour les routes /api/** (REST).
 * Les routes Web conservent l'authentification par session.
 */
@Component
public class JwtUtil {

    private final SecretKey signingKey;
    private final long expirationMs;

    public JwtUtil(
            @Value("${pressing.jwt.secret}") String secret,
            @Value("${pressing.jwt.expiration}") long expirationMs) {
        byte[] keyBytes = Base64.getDecoder().decode(secret);
        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
        this.expirationMs = expirationMs;
    }

    /**
     * Génère un token JWT signé avec l'email et le rôle de l'utilisateur.
     */
    public String genererToken(String email, String role) {
        Date maintenant = new Date();
        Date expiration = new Date(maintenant.getTime() + expirationMs);

        return Jwts.builder()
                .subject(email)
                .claim("role", role)
                .issuedAt(maintenant)
                .expiration(expiration)
                .signWith(signingKey)
                .compact();
    }

    /**
     * Extrait l'email (subject) d'un token JWT.
     */
    public String extraireEmail(String token) {
        return extraireClaims(token).getSubject();
    }

    /**
     * Extrait le rôle d'un token JWT.
     */
    public String extraireRole(String token) {
        return extraireClaims(token).get("role", String.class);
    }

    /**
     * Valide un token JWT : signature + expiration + cohérence avec le user.
     */
    public boolean estValide(String token, UserDetails userDetails) {
        try {
            String email = extraireEmail(token);
            return email.equals(userDetails.getUsername()) && !estExpire(token);
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    private boolean estExpire(String token) {
        return extraireClaims(token).getExpiration().before(new Date());
    }

    private Claims extraireClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
