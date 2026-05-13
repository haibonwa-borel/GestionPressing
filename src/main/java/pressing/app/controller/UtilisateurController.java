package pressing.app.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pressing.app.dto.PageResponse;
import pressing.app.dto.UtilisateurDTO;
import pressing.app.service.UtilisateurService;

/**
 * Controller REST pour les utilisateurs.
 * Utilise UtilisateurDTO pour la validation et la reponse.
 */
@RestController
@RequestMapping("/api/utilisateurs")
public class UtilisateurController {

    private final UtilisateurService service;

    public UtilisateurController(UtilisateurService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<PageResponse<UtilisateurDTO>> listerTous(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int taille) {
        return ResponseEntity.ok(service.listerAvecPagination(page, taille));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UtilisateurDTO> trouverParId(@PathVariable Long id) {
        return service.trouverParId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping(consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<UtilisateurDTO> creer(@Valid @ModelAttribute UtilisateurDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.creer(dto));
    }

    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<UtilisateurDTO> modifier(@PathVariable Long id, @Valid @ModelAttribute UtilisateurDTO dto) {
        return ResponseEntity.ok(service.modifier(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<java.util.Map<String, String>> supprimer(@PathVariable Long id) {
        if (service.supprimer(id)) {
            return ResponseEntity.ok(java.util.Collections.singletonMap("message", "Utilisateur supprime"));
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/recherche")
    public ResponseEntity<PageResponse<UtilisateurDTO>> rechercher(
            @RequestParam String terme,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int taille) {
        return ResponseEntity.ok(service.rechercherAvecPagination(terme, page, taille));
    }
}
