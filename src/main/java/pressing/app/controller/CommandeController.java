package pressing.app.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pressing.app.dto.CommandeDTO;
import pressing.app.dto.PageResponse;
import pressing.app.model.StatutCommande;
import pressing.app.model.TypeCommande;
import pressing.app.service.CommandeService;

import java.util.List;

/**
 * Controller REST pour les commandes.
 * Utilise CommandeDTO pour la validation et la reponse.
 */
@RestController
@RequestMapping("/api/commandes")
public class CommandeController {

    private final CommandeService service;

    public CommandeController(CommandeService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<PageResponse<CommandeDTO>> listerTous(
            @RequestParam(required = false) TypeCommande type,
            @RequestParam(required = false) StatutCommande statut,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int taille) {
        return ResponseEntity.ok(service.filtrerAvecPagination(type, statut, page, taille));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CommandeDTO> trouverParId(@PathVariable Long id) {
        return service.trouverParId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping(consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<CommandeDTO> creer(@Valid @ModelAttribute CommandeDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.creer(dto));
    }

    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<CommandeDTO> modifier(@PathVariable Long id, @Valid @ModelAttribute CommandeDTO dto) {
        return ResponseEntity.ok(service.modifier(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<java.util.Map<String, String>> supprimer(@PathVariable Long id) {
        if (service.supprimer(id)) {
            return ResponseEntity.ok(java.util.Collections.singletonMap("message", "Commande supprimee"));
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/utilisateur/{utilisateurId}")
    public ResponseEntity<List<CommandeDTO>> listerParUtilisateur(
            @PathVariable Long utilisateurId,
            @RequestParam(required = false) TypeCommande type,
            @RequestParam(required = false) StatutCommande statut) {
        return ResponseEntity.ok(service.filtrerParUtilisateur(utilisateurId, type, statut));
    }

    /**
     * Rechercher toutes les commandes contenant un vetement donne.
     * Ex: GET /api/commandes/vetement/3
     */
    @GetMapping("/vetement/{vetementId}")
    public ResponseEntity<List<CommandeDTO>> listerParVetement(@PathVariable Long vetementId) {
        return ResponseEntity.ok(service.trouverParVetement(vetementId));
    }
}
