package pressing.app.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import pressing.app.dto.PageResponse;
import pressing.app.dto.VetementDTO;
import pressing.app.model.CategorieVetement;
import pressing.app.service.FileStorageService;
import pressing.app.service.VetementService;

import java.util.List;

/**
 * Controller REST pour les vetements.
 * Utilise VetementDTO pour la validation et la reponse.
 */
@RestController
public class VetementController {

    private final VetementService service;
    private final FileStorageService fileStorageService;

    public VetementController(VetementService service, FileStorageService fileStorageService) {
        this.service = service;
        this.fileStorageService = fileStorageService;
    }

    @GetMapping("/api/vetements")
    public ResponseEntity<PageResponse<VetementDTO>> listerTous(
            @RequestParam(required = false) CategorieVetement categorie,
            @RequestParam(required = false) String recherche,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int taille) {
        return ResponseEntity.ok(service.filtrerAvecPagination(categorie, recherche, page, taille));
    }

    @GetMapping("/api/vetements/{id}")
    public ResponseEntity<VetementDTO> trouverParId(@PathVariable Long id) {
        return service.trouverParId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping(value = "/api/vetements", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<VetementDTO> creer(
            @Valid @ModelAttribute VetementDTO dto,
            @RequestParam(value = "photo", required = false) MultipartFile photo) {
        
        gererUploadPhoto(dto, photo);
        VetementDTO cree = service.creer(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(cree);
    }

    @PutMapping(value = "/api/vetements/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<VetementDTO> modifier(
            @PathVariable Long id,
            @Valid @ModelAttribute VetementDTO dto,
            @RequestParam(value = "photo", required = false) MultipartFile photo) {
        
        if (service.trouverParId(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        gererUploadPhoto(dto, photo);
        return ResponseEntity.ok(service.modifier(id, dto));
    }

    @DeleteMapping("/api/vetements/{id}")
    public ResponseEntity<java.util.Map<String, String>> supprimer(@PathVariable Long id) {
        if (service.supprimer(id)) {
            return ResponseEntity.ok(java.util.Collections.singletonMap("message", "Vetement supprime"));
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping(value = "/api/commandes/{commandeId}/vetements", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<VetementDTO> ajouterVetementACommande(
            @PathVariable Long commandeId,
            @Valid @ModelAttribute VetementDTO dto,
            @RequestParam(value = "photo", required = false) MultipartFile photo) {
        
        gererUploadPhoto(dto, photo);
        VetementDTO cree = service.creerPourCommande(commandeId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(cree);
    }

    @GetMapping("/api/commandes/{commandeId}/vetements")
    public ResponseEntity<List<VetementDTO>> listerVetementsDeCommande(@PathVariable Long commandeId) {
        return ResponseEntity.ok(service.listerParCommande(commandeId));
    }

    @PostMapping(value = "/api/vetements/recherche-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<List<VetementDTO>> rechercherParImage(@RequestParam("image") MultipartFile image) {
        return ResponseEntity.ok(service.rechercherParImage(image));
    }

    private void gererUploadPhoto(VetementDTO dto, MultipartFile photo) {
        if (photo != null && !photo.isEmpty()) {
            String fileName = fileStorageService.stockerFichier(photo);
            String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path("/uploads/")
                    .path(fileName)
                    .toUriString();
            dto.setPhotoUrl(fileDownloadUri);
        }
    }
}
