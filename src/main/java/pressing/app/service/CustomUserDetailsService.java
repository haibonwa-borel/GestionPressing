package pressing.app.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import pressing.app.model.Utilisateur;
import pressing.app.repository.UtilisateurRepository;

/**
 * Implementation personnalisee de UserDetailsService.
 * Charge les informations utilisateur depuis la base de donnees
 * pour l'authentification Spring Security.
 *
 * L'email est utilise comme identifiant de connexion.
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private static final Logger log = LoggerFactory.getLogger(CustomUserDetailsService.class);

    private final UtilisateurRepository utilisateurRepository;

    public CustomUserDetailsService(UtilisateurRepository utilisateurRepository) {
        this.utilisateurRepository = utilisateurRepository;
    }

    /**
     * Charge un utilisateur par son email (utilise comme username).
     *
     * @param email l'email de l'utilisateur
     * @return UserDetails contenant les informations de securite
     * @throws UsernameNotFoundException si l'utilisateur n'est pas trouve
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        log.debug("Tentative de chargement de l'utilisateur avec email: {}", email);

        Utilisateur utilisateur = utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("Echec d'authentification - utilisateur non trouve: {}", email);
                    return new UsernameNotFoundException("Utilisateur non trouve : " + email);
                });

        log.info("Utilisateur authentifie avec succes: {} (role: {})", email, utilisateur.getRole());

        return User.builder()
                .username(utilisateur.getEmail())
                .password(utilisateur.getMotDePasse())
                .roles(utilisateur.getRole())
                .accountExpired(false)
                .accountLocked(!utilisateur.isActif())
                .credentialsExpired(false)
                .disabled(!utilisateur.isActif())
                .build();
    }
}
