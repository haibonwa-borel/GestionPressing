package pressing.app.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pressing.app.dto.PageResponse;
import pressing.app.dto.VetementDTO;
import pressing.app.model.CategorieVetement;
import pressing.app.model.Vetement;
import pressing.app.repository.CommandeRepository;
import pressing.app.repository.VetementRepository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service metier pour les vetements.
 * Utilise JpaRepository et retourne des DTO.
 */
@Service
@Transactional
public class VetementService {

    private final VetementRepository vetementRepository;
    private final CommandeService commandeService;
    private final GeminiService geminiService;

    public VetementService(VetementRepository vetementRepository,
                           CommandeService commandeService,
                           GeminiService geminiService) {
        this.vetementRepository = vetementRepository;
        this.commandeService = commandeService;
        this.geminiService = geminiService;
    }

    /**
     * Creer un vetement independant sans l'associer a une commande.
     */
    public VetementDTO creer(VetementDTO dto) {
        Vetement entity = toEntity(dto);
        if (entity.getSku() == null) {
            entity.setSku(Vetement.genererSku());
        }
        Vetement saved = vetementRepository.save(entity);
        return toDTO(saved);
    }

    /**
     * Creer un vetement et l'associer directement a une commande.
     */
    public VetementDTO creerPourCommande(Long commandeId, VetementDTO dto) {
        Vetement entity = toEntity(dto);
        if (entity.getSku() == null) {
            entity.setSku(Vetement.genererSku());
        }
        Vetement saved = vetementRepository.save(entity);
        // Mettre a jour le prix de la commande via CommandeService
        commandeService.ajouterVetement(commandeId, saved);
        return toDTO(vetementRepository.findById(saved.getId()).orElse(saved));
    }

    @Transactional(readOnly = true)
    public Optional<VetementDTO> trouverParId(Long id) {
        return vetementRepository.findById(id).map(this::toDTO);
    }

    public VetementDTO modifier(Long id, VetementDTO dto) {
        if (!vetementRepository.existsById(id)) {
            throw new IllegalArgumentException("Vetement non trouve : " + id);
        }
        Vetement entity = toEntity(dto);
        entity.setId(id);
        // Conserver le SKU existant
        vetementRepository.findById(id).ifPresent(existing -> {
            if (entity.getSku() == null) entity.setSku(existing.getSku());
        });
        return toDTO(vetementRepository.save(entity));
    }

    public boolean supprimer(Long id) {
        if (!vetementRepository.existsById(id)) return false;
        vetementRepository.deleteById(id);
        return true;
    }

    @Transactional(readOnly = true)
    public List<VetementDTO> listerParCommande(Long commandeId) {
        return vetementRepository.findByCommandeId(commandeId).stream()
                .map(this::toDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PageResponse<VetementDTO> filtrerAvecPagination(CategorieVetement categorie, String recherche,
                                                            int page, int taille) {
        List<Vetement> resultats;
        if (recherche != null && !recherche.isBlank()) {
            resultats = vetementRepository.rechercher(recherche).stream()
                    .filter(v -> categorie == null || v.getCategorie() == categorie)
                    .collect(Collectors.toList());
        } else if (categorie != null) {
            resultats = vetementRepository.findByCategorie(categorie);
        } else {
            Page<Vetement> pageResult = vetementRepository.findAll(PageRequest.of(page, taille));
            return new PageResponse<>(
                    pageResult.getContent().stream().map(this::toDTO).collect(Collectors.toList()),
                    page, taille, pageResult.getTotalElements());
        }

        long total = resultats.size();
        List<VetementDTO> slice = resultats.stream()
                .skip((long) page * taille).limit(taille)
                .map(this::toDTO).collect(Collectors.toList());
        return new PageResponse<>(slice, page, taille, total);
    }

    /**
     * Recherche par similarité d'image (Simulation IA).
     */
    @Transactional(readOnly = true)
    public List<VetementDTO> rechercherParImage(org.springframework.web.multipart.MultipartFile image) {
        try {
            byte[] bytes = image.getBytes();
            Map<String, String> analyse = geminiService.analyserImage(bytes);
            
            String catStr = analyse.get("categorie");
            String couleur = analyse.get("couleur");
            
            if (catStr != null) {
                CategorieVetement cat = CategorieVetement.valueOf(catStr);
                List<Vetement> matches = vetementRepository.findByCategorie(cat);
                
                if (couleur != null) {
                    final String c = couleur.toLowerCase();
                    matches = matches.stream()
                            .filter(v -> v.getCouleur() != null && v.getCouleur().toLowerCase().contains(c))
                            .collect(Collectors.toList());
                }
                
                return matches.stream().map(this::toDTO).collect(Collectors.toList());
            }
        } catch (Exception e) {
            System.err.println("Erreur recherche image : " + e.getMessage());
        }
        
        // Fallback simulation si Gemini échoue ou ne trouve rien
        String fileName = image.getOriginalFilename() != null ? image.getOriginalFilename().toLowerCase() : "";
        if (fileName.contains("chemise")) {
            return vetementRepository.findByCategorie(CategorieVetement.CHEMISE).stream()
                    .map(this::toDTO).collect(Collectors.toList());
        }
        
        return vetementRepository.findAll(PageRequest.of(0, 8)).getContent().stream()
                .map(this::toDTO).collect(Collectors.toList());
    }

    // ======== MAPPING ========
    public VetementDTO toDTO(Vetement e) {
        VetementDTO dto = new VetementDTO();
        dto.setId(e.getId());
        dto.setSku(e.getSku());
        dto.setDescription(e.getDescription());
        dto.setCategorie(e.getCategorie());
        dto.setCouleur(e.getCouleur());
        dto.setCodeCouleur(e.getCodeCouleur());
        dto.setPhotoUrl(e.getPhotoUrl());
        if (e.getCommandes() != null) {
            dto.setCommandeIds(e.getCommandes().stream()
                    .map(pressing.app.model.Commande::getId)
                    .collect(Collectors.toList()));
        }
        return dto;
    }

    public Vetement toEntity(VetementDTO dto) {
        Vetement e = new Vetement();
        e.setId(dto.getId());
        e.setSku(dto.getSku());
        e.setDescription(dto.getDescription());
        e.setCategorie(dto.getCategorie());
        e.setCouleur(dto.getCouleur());
        e.setCodeCouleur(dto.getCodeCouleur());
        e.setPhotoUrl(dto.getPhotoUrl());
        return e;
    }
}
