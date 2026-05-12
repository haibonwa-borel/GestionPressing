package pressing.app.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import pressing.app.repository.CommandeRepository;
import pressing.app.repository.UtilisateurRepository;
import pressing.app.repository.VetementRepository;

@Controller
public class HomeController {

    private final UtilisateurRepository utilisateurRepository;
    private final CommandeRepository commandeRepository;
    private final VetementRepository vetementRepository;

    public HomeController(UtilisateurRepository utilisateurRepository,
                          CommandeRepository commandeRepository,
                          VetementRepository vetementRepository) {
        this.utilisateurRepository = utilisateurRepository;
        this.commandeRepository = commandeRepository;
        this.vetementRepository = vetementRepository;
    }

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("activeMenu", "home");
        model.addAttribute("totalClients", utilisateurRepository.count());
        model.addAttribute("totalCommandes", commandeRepository.count());
        model.addAttribute("totalVetements", vetementRepository.count());
        return "index";
    }
}
