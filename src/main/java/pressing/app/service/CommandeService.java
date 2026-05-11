package pressing.app.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pressing.app.dto.CommandeDTO;
import pressing.app.dto.PageResponse;
import pressing.app.dto.VetementDTO;
import pressing.app.model.Commande;
import pressing.app.model.StatutCommande;
import pressing.app.model.TypeCommande;
import pressing.app.model.Vetement;
import pressing.app.repository.CommandeRepository;
import pressing.app.repository.VetementRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service metier pour les commandes.
 * Utilise JpaRepository. La relation Many-to-Many est geree par JPA.
 * Cascade : quand on supprime une Commande, la table de jointure est videe automatiquement.
 */
@Service
@Transactional
public class CommandeService {

    private final CommandeRepository commandeRepository;
    private final VetementRepository vetementRepository;
    private final NotificationService notificationService;

    private static final double PRIX_BASE = 1000.0;

    public CommandeService(CommandeRepository commandeRepository,
                           VetementRepository vetementRepository,
                           NotificationService notificationService) {
        this.commandeRepository = commandeRepository;
        this.vetementRepository = vetementRepository;
        this.notificationService = notificationService;
    }

    private double calculerPrix(TypeCommande type, int nbVetements) {
        double mult = switch (type) {
            case RAPIDE -> 1.5;
            case EXPRESS -> 2.0;
            default -> 1.0;
        };
        return nbVetements * PRIX_BASE * mult;
    }

    private LocalDateTime calculerDateRetrait(TypeCommande type, LocalDateTime base) {
        if (base == null) base = LocalDateTime.now();
        return switch (type) {
            case NORMAL -> base.plusDays(7);
            case RAPIDE -> base.plusDays(4);
            case EXPRESS -> base.plusDays(1);
        };
    }

    public CommandeDTO creer(CommandeDTO dto) {
        Commande entity = new Commande();
        entity.setType(dto.getType());
        entity.setStatut(dto.getStatut() != null ? dto.getStatut() : StatutCommande.EN_ATTENTE);
        entity.setUtilisateurId(dto.getUtilisateurId());
        entity.setDateCreation(LocalDateTime.now());
        entity.setDateLivraison(calculerDateRetrait(dto.getType(), entity.getDateCreation()));

        // Associer des vêtements existants si des IDs sont fournis
        if (dto.getVetementIds() != null && !dto.getVetementIds().isEmpty()) {
            List<Vetement> existants = vetementRepository.findAllById(dto.getVetementIds());
            entity.setVetements(existants);
        } else {
            entity.setVetements(new ArrayList<>());
        }

        int nbVetements = entity.getVetements().size();
        entity.setPrixTotal(calculerPrix(entity.getType(), nbVetements));

        Commande saved = commandeRepository.save(entity);

        // Envoi automatique de la facture par mail
        try {
            notificationService.envoyerFacture(saved.getId());
        } catch (Exception e) {
            System.err.println("Echec envoi auto facture : " + e.getMessage());
        }

        return toDTO(saved);
    }

    @Transactional(readOnly = true)
    public Optional<CommandeDTO> trouverParId(Long id) {
        return commandeRepository.findById(id).map(this::toDTO);
    }

    public CommandeDTO modifier(Long id, CommandeDTO dto) {
        Commande entity = commandeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Commande non trouvee : " + id));
        entity.setType(dto.getType());
        if (dto.getStatut() != null) entity.setStatut(dto.getStatut());
        entity.setUtilisateurId(dto.getUtilisateurId());
        if (dto.getDateCreation() != null) entity.setDateCreation(dto.getDateCreation());
        entity.setDateLivraison(calculerDateRetrait(entity.getType(), entity.getDateCreation()));
        int nbVetements = entity.getVetements() != null ? entity.getVetements().size() : 0;
        entity.setPrixTotal(calculerPrix(entity.getType(), nbVetements));
        return toDTO(commandeRepository.save(entity));
    }

    /**
     * Suppression avec cascade :
     * JPA supprime automatiquement les lignes dans commande_vetement
     * grace au @JoinTable sur Commande.
     */
    public boolean supprimer(Long id) {
        if (!commandeRepository.existsById(id)) return false;
        commandeRepository.deleteById(id);
        return true;
    }

    /**
     * Ajouter un vetement existant a une commande (mise a jour Many-to-Many).
     */
    public CommandeDTO ajouterVetement(Long commandeId, Vetement vetement) {
        Commande commande = commandeRepository.findById(commandeId)
                .orElseThrow(() -> new IllegalArgumentException("Commande non trouvee : " + commandeId));

        if (!commande.getVetements().contains(vetement)) {
            commande.getVetements().add(vetement);
        }
        int nbVetements = commande.getVetements().size();
        commande.setPrixTotal(calculerPrix(commande.getType(), nbVetements));
        commande.setDateLivraison(calculerDateRetrait(commande.getType(), commande.getDateCreation()));
        return toDTO(commandeRepository.save(commande));
    }

    @Transactional(readOnly = true)
    public PageResponse<CommandeDTO> filtrerAvecPagination(TypeCommande type, StatutCommande statut, int page, int taille) {
        List<Commande> resultats;
        if (type != null && statut != null) {
            resultats = commandeRepository.findByTypeAndStatut(type, statut);
        } else if (type != null) {
            resultats = commandeRepository.findByType(type);
        } else if (statut != null) {
            resultats = commandeRepository.findByStatut(statut);
        } else {
            Page<Commande> pageResult = commandeRepository.findAll(PageRequest.of(page, taille));
            List<CommandeDTO> contenu = pageResult.getContent().stream().map(this::toDTO).collect(Collectors.toList());
            return new PageResponse<>(contenu, page, taille, pageResult.getTotalElements());
        }

        long total = resultats.size();
        List<CommandeDTO> slice = resultats.stream()
                .skip((long) page * taille).limit(taille)
                .map(this::toDTO).collect(Collectors.toList());
        return new PageResponse<>(slice, page, taille, total);
    }

    @Transactional(readOnly = true)
    public List<CommandeDTO> filtrerParUtilisateur(Long utilisateurId, TypeCommande type, StatutCommande statut) {
        return commandeRepository.findByUtilisateurId(utilisateurId).stream()
                .filter(c -> type == null || c.getType() == type)
                .filter(c -> statut == null || c.getStatut() == statut)
                .map(this::toDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CommandeDTO> trouverParVetement(Long vetementId) {
        return commandeRepository.findByVetementId(vetementId).stream()
                .map(this::toDTO).collect(Collectors.toList());
    }

    // ======== MAPPING ========
    private CommandeDTO toDTO(Commande e) {
        CommandeDTO dto = new CommandeDTO();
        dto.setId(e.getId());
        dto.setType(e.getType());
        dto.setStatut(e.getStatut());
        dto.setDateCreation(e.getDateCreation());
        dto.setDateLivraison(e.getDateLivraison());
        dto.setPrixTotal(e.getPrixTotal());
        dto.setUtilisateurId(e.getUtilisateurId());

        if (e.getVetements() != null) {
            dto.setVetementIds(e.getVetements().stream()
                    .map(Vetement::getId)
                    .collect(Collectors.toList()));
            dto.setVetements(e.getVetements().stream()
                    .map(this::mapToVetementDTO)
                    .collect(Collectors.toList()));
        }
        return dto;
    }

    private VetementDTO mapToVetementDTO(Vetement v) {
        VetementDTO vDto = new VetementDTO();
        vDto.setId(v.getId());
        vDto.setSku(v.getSku());
        vDto.setDescription(v.getDescription());
        vDto.setCategorie(v.getCategorie());
        vDto.setCouleur(v.getCouleur());
        vDto.setCodeCouleur(v.getCodeCouleur());
        vDto.setPhotoUrl(v.getPhotoUrl());
        return vDto;
    }
}
