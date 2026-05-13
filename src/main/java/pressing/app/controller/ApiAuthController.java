package pressing.app.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import pressing.app.security.JwtUtil;

import java.util.Map;

/**
 * Controller REST pour l'authentification API.
 * POST /api/login  → authentifie et retourne un JWT Bearer token
 * POST /api/logout → stateless, le client supprime son token
 *
 * Utilisation dans Postman :
 *  1. POST /api/login → copier la valeur du champ "token"
 *  2. Dans les requêtes suivantes → Authorization > Bearer Token → coller le token
 */
@RestController
@RequestMapping("/api")
public class ApiAuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    public ApiAuthController(AuthenticationManager authenticationManager, JwtUtil jwtUtil) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
    }

    /**
     * Login REST — retourne un JWT Bearer token.
     *
     * Corps attendu (application/x-www-form-urlencoded) :
     *   email=admin@pressing.com&motDePasse=password123
     */
    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(
            @RequestParam String email,
            @RequestParam String motDePasse) {

        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, motDePasse)
            );

            String role = auth.getAuthorities().stream()
                    .map(a -> a.getAuthority())
                    .findFirst()
                    .orElse("INCONNU");

            String token = jwtUtil.genererToken(email, role);

            return ResponseEntity.ok(Map.of(
                    "token", token,
                    "type", "Bearer",
                    "email", email,
                    "role", role,
                    "expiresIn", "86400s (24h)"
            ));

        } catch (BadCredentialsException e) {
            return ResponseEntity.status(401).body(Map.of(
                    "error", "Identifiants invalides"
            ));
        }
    }

    /**
     * Logout REST — côté stateless, le client supprime son token.
     * Pas de session serveur à invalider avec JWT.
     */
    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout() {
        return ResponseEntity.ok(Map.of(
                "message", "Déconnexion effectuée. Supprimez votre token côté client."
        ));
    }
}
