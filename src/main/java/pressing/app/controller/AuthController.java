package pressing.app.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controller pour la page de connexion personnalisee.
 */
@Controller
public class AuthController {

    /**
     * Affiche la page de connexion.
     * Les parametres 'error' et 'logout' sont geres automatiquement
     * par Spring Security et affiches dans la vue Thymeleaf.
     */
    @GetMapping("/login")
    public String login() {
        return "login";
    }
}
