package pressing.app.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import pressing.app.dto.UtilisateurDTO;
import pressing.app.repository.CommandeRepository;
import pressing.app.repository.UtilisateurRepository;
import pressing.app.repository.VetementRepository;
import pressing.app.service.UtilisateurService;

@Controller
public class UIController {

    private final UtilisateurRepository utilisateurRepository;
    private final CommandeRepository commandeRepository;
    private final VetementRepository vetementRepository;
    private final UtilisateurService utilisateurService;

    public UIController(UtilisateurRepository utilisateurRepository,
                        CommandeRepository commandeRepository,
                        VetementRepository vetementRepository,
                        UtilisateurService utilisateurService) {
        this.utilisateurRepository = utilisateurRepository;
        this.commandeRepository = commandeRepository;
        this.vetementRepository = vetementRepository;
        this.utilisateurService = utilisateurService;
    }

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("activeMenu", "home");
        model.addAttribute("totalClients", utilisateurRepository.count());
        model.addAttribute("totalCommandes", commandeRepository.count());
        model.addAttribute("totalVetements", vetementRepository.count());
        return "index";
    }

    @GetMapping("/utilisateurs")
    public String utilisateurs(Model model) {
        model.addAttribute("activeMenu", "utilisateurs");
        model.addAttribute("utilisateurs", utilisateurRepository.findAll());
        return "utilisateurs";
    }

    @GetMapping("/commandes")
    public String commandes(Model model) {
        model.addAttribute("activeMenu", "commandes");
        model.addAttribute("commandes", commandeRepository.findAll());
        model.addAttribute("utilisateurs", utilisateurRepository.findAll());
        model.addAttribute("vetements", vetementRepository.findAll());
        return "commandes";
    }

    @GetMapping("/vetements")
    public String vetements(Model model) {
        model.addAttribute("activeMenu", "vetements");
        model.addAttribute("vetements", vetementRepository.findAll());
        return "vetements";
    }

    @PostMapping("/utilisateurs")
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
}
