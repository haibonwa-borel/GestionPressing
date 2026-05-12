package pressing.app.controller;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import pressing.app.dto.CommandeDTO;
import pressing.app.model.Commande;
import pressing.app.repository.CommandeRepository;
import pressing.app.repository.UtilisateurRepository;
import pressing.app.repository.VetementRepository;
import pressing.app.service.CommandeService;
import pressing.app.service.NotificationService;

import java.nio.file.Path;
import java.nio.file.Paths;

@Controller
@RequestMapping("/commandes")
public class CommandeUIController {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CommandeUIController.class);

    private final CommandeRepository commandeRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final VetementRepository vetementRepository;
    private final CommandeService commandeService;
    private final NotificationService notificationService;

    public CommandeUIController(CommandeRepository commandeRepository,
                                UtilisateurRepository utilisateurRepository,
                                VetementRepository vetementRepository,
                                CommandeService commandeService,
                                NotificationService notificationService) {
        this.commandeRepository = commandeRepository;
        this.utilisateurRepository = utilisateurRepository;
        this.vetementRepository = vetementRepository;
        this.commandeService = commandeService;
        this.notificationService = notificationService;
    }

    @GetMapping
    public String commandes(Model model) {
        model.addAttribute("activeMenu", "commandes");
        model.addAttribute("commandes", commandeRepository.findAll());
        model.addAttribute("utilisateurs", utilisateurRepository.findAll());
        model.addAttribute("vetements", vetementRepository.findAll());
        return "commandes";
    }

    @PostMapping
    public String creerCommande(@ModelAttribute CommandeDTO dto) {
        // 1. Créer la commande (Transactionnelle)
        CommandeDTO saved = commandeService.creer(dto);
        
        // 2. Déclencher l'envoi de la facture (Hors transaction de création)
        try {
            notificationService.envoyerFacture(saved.getId());
        } catch (Exception e) {
            System.err.println("Erreur lors du déclenchement de la notification : " + e.getMessage());
        }
        
        return "redirect:/commandes";
    }

    @PutMapping("/{id}")
    public String modifierCommande(
            @PathVariable Long id,
            @ModelAttribute CommandeDTO dto) {
        commandeService.modifier(id, dto);
        log.info(" ACTION : Commande ID [{}] modifiée.", id);
        return "redirect:/commandes";
    }

    @DeleteMapping("/{id}")
    public String supprimerCommande(@PathVariable Long id) {
        commandeService.supprimer(id);
        log.warn(" ACTION : Commande ID [{}] supprimée.", id);
        return "redirect:/commandes";
    }

    @GetMapping("/{id}/facture")
    public ResponseEntity<Resource> telechargerFacture(@PathVariable Long id) {
        Commande commande = commandeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Commande non trouvée"));

        if (commande.getFactureUrl() == null) {
            return ResponseEntity.notFound().build();
        }

        try {
            Path path = Paths.get("uploads").resolve(commande.getFactureUrl());
            Resource resource = new UrlResource(path.toUri());

            if (resource.exists() || resource.isReadable()) {
                return ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_PDF)
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
