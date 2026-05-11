package pressing.app.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import pressing.app.dto.CommandeDTO;
import pressing.app.dto.UtilisateurDTO;
import pressing.app.dto.VetementDTO;
import pressing.app.repository.CommandeRepository;
import pressing.app.repository.UtilisateurRepository;
import pressing.app.repository.VetementRepository;
import pressing.app.service.CommandeService;
import pressing.app.service.FileStorageService;
import pressing.app.service.UtilisateurService;
import pressing.app.service.VetementService;

@Controller
public class UIController {

    private final UtilisateurRepository utilisateurRepository;
    private final CommandeRepository commandeRepository;
    private final VetementRepository vetementRepository;
    private final UtilisateurService utilisateurService;
    private final VetementService vetementService;
    private final CommandeService commandeService;
    private final FileStorageService fileStorageService;

    public UIController(UtilisateurRepository utilisateurRepository,
                        CommandeRepository commandeRepository,
                        VetementRepository vetementRepository,
                        UtilisateurService utilisateurService,
                        VetementService vetementService,
                        CommandeService commandeService,
                        FileStorageService fileStorageService) {
        this.utilisateurRepository = utilisateurRepository;
        this.commandeRepository = commandeRepository;
        this.vetementRepository = vetementRepository;
        this.utilisateurService = utilisateurService;
        this.vetementService = vetementService;
        this.commandeService = commandeService;
        this.fileStorageService = fileStorageService;
    }

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("activeMenu", "home");
        model.addAttribute("totalClients", utilisateurRepository.count());
        model.addAttribute("totalCommandes", commandeRepository.count());
        model.addAttribute("totalVetements", vetementRepository.count());
        return "index";
    }

    // --- UTILISATEURS ---

    @GetMapping("/utilisateurs")
    public String utilisateurs(Model model) {
        model.addAttribute("activeMenu", "utilisateurs");
        model.addAttribute("utilisateurs", utilisateurRepository.findAll());
        return "utilisateurs";
    }

    @PostMapping("/utilisateurs/add")
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
        return "redirect:/utilisateurs";
    }

    @PostMapping("/utilisateurs/edit")
    public String modifierUtilisateur(
            @RequestParam Long id,
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
        return "redirect:/utilisateurs";
    }

    @PostMapping("/utilisateurs/{id}/delete")
    public String supprimerUtilisateur(@PathVariable Long id) {
        utilisateurService.supprimer(id);
        return "redirect:/utilisateurs";
    }

    // --- VETEMENTS ---

    @GetMapping("/vetements")
    public String vetements(Model model) {
        model.addAttribute("activeMenu", "vetements");
        model.addAttribute("vetements", vetementRepository.findAll());
        return "vetements";
    }

    @PostMapping("/vetements/add")
    public String creerVetement(
            @ModelAttribute VetementDTO dto,
            @RequestParam(value = "photo", required = false) MultipartFile photo) {
        gererUploadPhoto(dto, photo);
        vetementService.creer(dto);
        return "redirect:/vetements";
    }

    @PostMapping("/vetements/edit")
    public String modifierVetement(
            @RequestParam Long id,
            @ModelAttribute VetementDTO dto,
            @RequestParam(value = "photo", required = false) MultipartFile photo) {
        gererUploadPhoto(dto, photo);
        vetementService.modifier(id, dto);
        return "redirect:/vetements";
    }

    @PostMapping("/vetements/{id}/delete")
    public String supprimerVetement(@PathVariable Long id) {
        vetementService.supprimer(id);
        return "redirect:/vetements";
    }

    // --- COMMANDES ---

    @GetMapping("/commandes")
    public String commandes(Model model) {
        model.addAttribute("activeMenu", "commandes");
        model.addAttribute("commandes", commandeRepository.findAll());
        model.addAttribute("utilisateurs", utilisateurRepository.findAll());
        model.addAttribute("vetements", vetementRepository.findAll());
        return "commandes";
    }

    @PostMapping("/commandes/add")
    public String creerCommande(@ModelAttribute CommandeDTO dto) {
        commandeService.creer(dto);
        return "redirect:/commandes";
    }

    @PostMapping("/commandes/edit")
    public String modifierCommande(
            @RequestParam Long id,
            @ModelAttribute CommandeDTO dto) {
        commandeService.modifier(id, dto);
        return "redirect:/commandes";
    }

    @PostMapping("/commandes/{id}/delete")
    public String supprimerCommande(@PathVariable Long id) {
        commandeService.supprimer(id);
        return "redirect:/commandes";
    }

    // --- UTILITAIRE ---

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
