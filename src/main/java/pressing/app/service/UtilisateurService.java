package pressing.app.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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
 */
@Service
@Transactional
public class UtilisateurService {

    private final UtilisateurRepository repository;

    public UtilisateurService(UtilisateurRepository repository) {
        this.repository = repository;
    }

    public UtilisateurDTO creer(UtilisateurDTO dto) {
        if (repository.findByEmail(dto.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Un utilisateur avec cet email existe deja");
        }
        if (dto.getTelephone() != null && !dto.getTelephone().isBlank()) {
            if (repository.findByTelephone(dto.getTelephone()).isPresent()) {
                throw new IllegalArgumentException("Un utilisateur avec ce numero de telephone existe deja");
            }
        }
        return toDTO(repository.save(toEntity(dto)));
    }

    @Transactional(readOnly = true)
    public Optional<UtilisateurDTO> trouverParId(Long id) {
        return repository.findById(id).map(this::toDTO);
    }

    public UtilisateurDTO modifier(Long id, UtilisateurDTO dto) {
        Utilisateur existant = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur non trouve"));

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
        if (dto.getMotDePasse() == null || dto.getMotDePasse().isBlank()) {
            entity.setMotDePasse(existant.getMotDePasse());
        }
        return toDTO(repository.save(entity));
    }

    public boolean supprimer(Long id) {
        if (!repository.existsById(id)) return false;
        repository.deleteById(id);
        return true;
    }

    @Transactional(readOnly = true)
    public PageResponse<UtilisateurDTO> listerAvecPagination(int page, int taille) {
        Page<Utilisateur> pageResult = repository.findAll(PageRequest.of(page, taille));
        List<UtilisateurDTO> contenu = pageResult.getContent().stream()
                .map(this::toDTO).collect(Collectors.toList());
        return new PageResponse<>(contenu, page, taille, pageResult.getTotalElements());
    }

    @Transactional(readOnly = true)
    public PageResponse<UtilisateurDTO> rechercherAvecPagination(String terme, int page, int taille) {
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
        return e;
    }
}
