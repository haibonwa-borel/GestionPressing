package pressing.app.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import pressing.app.dto.UtilisateurDTO;
import pressing.app.repository.UtilisateurRepository;
import pressing.app.service.UtilisateurService;

@Controller
@RequestMapping("/utilisateurs")
public class UtilisateurUIController {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(UtilisateurUIController.class);

    private final UtilisateurRepository utilisateurRepository;
    private final UtilisateurService utilisateurService;

    public UtilisateurUIController(UtilisateurRepository utilisateurRepository,
                                   UtilisateurService utilisateurService) {
        this.utilisateurRepository = utilisateurRepository;
        this.utilisateurService = utilisateurService;
    }

    @GetMapping
    public String utilisateurs(Model model) {
        model.addAttribute("activeMenu", "utilisateurs");
        model.addAttribute("utilisateurs", utilisateurRepository.findAll());
        return "utilisateurs";
    }

    @PostMapping
    public String creerUtilisateur(
            @RequestParam String nom,
            @RequestParam String prenom,
            @RequestParam String email,
            @RequestParam String motDePasse,
            @RequestParam(required = false) String telephone) {
        UtilisateurDTO dto = new UtilisateurDTO();
        dto.setNom(nom);
        dto.setPrenom(prenom);
        dto.setEmail(email);
        dto.setMotDePasse(motDePasse);
        dto.setTelephone(telephone);
        utilisateurService.creer(dto);
        log.info(" ACTION : Nouvel utilisateur créé : {} ({})", dto.getEmail(), dto.getNom());
        return "redirect:/utilisateurs";
    }

    @PutMapping("/{id}")
    public String modifierUtilisateur(
            @PathVariable Long id,
            @RequestParam String nom,
            @RequestParam String prenom,
            @RequestParam String email,
            @RequestParam(required = false) String telephone) {
        UtilisateurDTO dto = new UtilisateurDTO();
        dto.setNom(nom);
        dto.setPrenom(prenom);
        dto.setEmail(email);
        dto.setTelephone(telephone);
        utilisateurService.modifier(id, dto);
        log.info(" ACTION : Utilisateur ID [{}] modifié : {}", id, dto.getEmail());
        return "redirect:/utilisateurs";
    }

    @DeleteMapping("/{id}")
    public String supprimerUtilisateur(@PathVariable Long id) {
        utilisateurService.supprimer(id);
        log.warn(" ACTION : Utilisateur ID [{}] supprimé.", id);
        return "redirect:/utilisateurs";
    }
}
