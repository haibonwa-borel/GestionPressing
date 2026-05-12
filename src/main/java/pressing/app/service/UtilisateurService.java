package pressing.app.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pressing.app.dto.PageResponse;
import pressing.app.dto.UtilisateurDTO;
import pressing.app.model.Utilisateur;
import pressing.app.repository.UtilisateurRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service metier pour les utilisateurs.
 * Utilise JpaRepository et retourne des DTO.
 * Les mots de passe sont encodes avec BCrypt (bonne pratique de securite).
 */
@Service
@Transactional
public class UtilisateurService {

    private static final Logger log = LoggerFactory.getLogger(UtilisateurService.class);

    private final UtilisateurRepository repository;
    private final PasswordEncoder passwordEncoder;

    public UtilisateurService(UtilisateurRepository repository, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    public UtilisateurDTO creer(UtilisateurDTO dto) {
        log.info("Creation d'un nouvel utilisateur avec email: {}", dto.getEmail());

        if (repository.findByEmail(dto.getEmail()).isPresent()) {
            log.warn("Tentative de creation avec un email existant: {}", dto.getEmail());
            throw new IllegalArgumentException("Un utilisateur avec cet email existe deja");
        }
        if (dto.getTelephone() != null && !dto.getTelephone().isBlank()) {
            if (repository.findByTelephone(dto.getTelephone()).isPresent()) {
                log.warn("Tentative de creation avec un telephone existant: {}", dto.getTelephone());
                throw new IllegalArgumentException("Un utilisateur avec ce numero de telephone existe deja");
            }
        }

        Utilisateur entity = toEntity(dto);
        // Encodage du mot de passe avec BCrypt avant persistance
        entity.setMotDePasse(passwordEncoder.encode(dto.getMotDePasse()));
        // Role par defaut : CLIENT
        if (dto.getRole() == null || dto.getRole().isBlank()) {
            entity.setRole("CLIENT");
        }

        Utilisateur saved = repository.save(entity);
        log.info("Utilisateur cree avec succes - ID: {}, email: {}", saved.getId(), saved.getEmail());
        return toDTO(saved);
    }

    @Transactional(readOnly = true)
    public Optional<UtilisateurDTO> trouverParId(Long id) {
        log.debug("Recherche utilisateur par ID: {}", id);
        return repository.findById(id).map(this::toDTO);
    }

    public UtilisateurDTO modifier(Long id, UtilisateurDTO dto) {
        log.info("Modification de l'utilisateur ID: {}", id);

        Utilisateur existant = repository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Utilisateur non trouve pour modification - ID: {}", id);
                    return new IllegalArgumentException("Utilisateur non trouve");
                });

        if (!existant.getEmail().equalsIgnoreCase(dto.getEmail())) {
            if (repository.findByEmail(dto.getEmail()).isPresent()) {
                throw new IllegalArgumentException("Un utilisateur avec cet email existe deja");
            }
        }

        String telNouveau = dto.getTelephone();
        if (telNouveau != null && !telNouveau.isBlank() && !telNouveau.equals(existant.getTelephone())) {
            if (repository.findByTelephone(telNouveau).isPresent()) {
                throw new IllegalArgumentException("Un utilisateur avec ce numero de telephone existe deja");
            }
        }

        Utilisateur entity = toEntity(dto);
        entity.setId(id);

        // Si un nouveau mot de passe est fourni, l'encoder avec BCrypt
        if (dto.getMotDePasse() != null && !dto.getMotDePasse().isBlank()) {
            entity.setMotDePasse(passwordEncoder.encode(dto.getMotDePasse()));
        } else {
            // Conserver l'ancien mot de passe
            entity.setMotDePasse(existant.getMotDePasse());
        }

        // Conserver le role existant si non fourni
        if (dto.getRole() == null || dto.getRole().isBlank()) {
            entity.setRole(existant.getRole());
        }

        UtilisateurDTO result = toDTO(repository.save(entity));
        log.info("Utilisateur modifie avec succes - ID: {}", id);
        return result;
    }

    public boolean supprimer(Long id) {
        log.info("Suppression de l'utilisateur ID: {}", id);
        if (!repository.existsById(id)) {
            log.warn("Tentative de suppression d'un utilisateur inexistant - ID: {}", id);
            return false;
        }
        repository.deleteById(id);
        log.info("Utilisateur supprime avec succes - ID: {}", id);
        return true;
    }

    @Transactional(readOnly = true)
    public PageResponse<UtilisateurDTO> listerAvecPagination(int page, int taille) {
        log.debug("Liste des utilisateurs - page: {}, taille: {}", page, taille);
        Page<Utilisateur> pageResult = repository.findAll(PageRequest.of(page, taille));
        List<UtilisateurDTO> contenu = pageResult.getContent().stream()
                .map(this::toDTO).collect(Collectors.toList());
        return new PageResponse<>(contenu, page, taille, pageResult.getTotalElements());
    }

    @Transactional(readOnly = true)
    public PageResponse<UtilisateurDTO> rechercherAvecPagination(String terme, int page, int taille) {
        log.debug("Recherche utilisateurs - terme: '{}', page: {}, taille: {}", terme, page, taille);
        String t = terme.toLowerCase();
        List<Utilisateur> tous = repository.findAll();
        List<UtilisateurDTO> resultats = tous.stream()
                .filter(u -> u.getNom().toLowerCase().contains(t)
                        || u.getPrenom().toLowerCase().contains(t)
                        || u.getEmail().toLowerCase().contains(t))
                .map(this::toDTO)
                .collect(Collectors.toList());
        long total = resultats.size();
        List<UtilisateurDTO> slice = resultats.stream()
                .skip((long) page * taille).limit(taille).collect(Collectors.toList());
        return new PageResponse<>(slice, page, taille, total);
    }

    // ======== MAPPING ========
    private UtilisateurDTO toDTO(Utilisateur e) {
        UtilisateurDTO dto = new UtilisateurDTO();
        dto.setId(e.getId());
        dto.setNom(e.getNom());
        dto.setPrenom(e.getPrenom());
        dto.setEmail(e.getEmail());
        dto.setTelephone(e.getTelephone());
        dto.setRole(e.getRole());
        // motDePasse intentionnellement omis (WRITE_ONLY)
        return dto;
    }

    private Utilisateur toEntity(UtilisateurDTO dto) {
        Utilisateur e = new Utilisateur();
        e.setId(dto.getId());
        e.setNom(dto.getNom());
        e.setPrenom(dto.getPrenom());
        e.setEmail(dto.getEmail());
        e.setTelephone(dto.getTelephone());
        e.setMotDePasse(dto.getMotDePasse());
        if (dto.getRole() != null && !dto.getRole().isBlank()) {
            e.setRole(dto.getRole());
        }
        return e;
    }
}
