package pressing.app.controller;

import org.springframework.web.bind.annotation.*;
import pressing.app.repository.CommandeRepository;
import pressing.app.repository.UtilisateurRepository;
import pressing.app.repository.VetementRepository;
import pressing.app.service.GeminiService;

import java.util.Collections;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final GeminiService geminiService;
    private final UtilisateurRepository utilisateurRepository;
    private final CommandeRepository commandeRepository;
    private final VetementRepository vetementRepository;

    public ChatController(UtilisateurRepository utilisateurRepository,
                          CommandeRepository commandeRepository,
                          VetementRepository vetementRepository,
                          GeminiService geminiService) {
        this.utilisateurRepository = utilisateurRepository;
        this.commandeRepository = commandeRepository;
        this.vetementRepository = vetementRepository;
        this.geminiService = geminiService;
    }

    @PostMapping
    public Map<String, Object> chat(@RequestBody Map<String, String> request) {
        String message = request.get("message");
        if (message == null || message.isEmpty()) {
            return Collections.singletonMap("error", "Message vide");
        }

        // Build Database Context
        StringBuilder context = new StringBuilder();
        context.append("Tu es l'assistant intelligent de l'application de gestion de pressing. Voici les données actuelles de la base de données :\n");
        
        context.append("\n--- CLIENTS ---\n");
        utilisateurRepository.findAll().forEach(u -> 
            context.append(String.format("ID: %d | Nom: %s %s | Email: %s | Tel: %s\n", u.getId(), u.getPrenom(), u.getNom(), u.getEmail(), u.getTelephone()))
        );

        context.append("\n--- VÊTEMENTS ---\n");
        vetementRepository.findAll().forEach(v -> 
            context.append(String.format("ID: %d | Catégorie: %s | Description: %s | Couleur: %s | SKU: %s\n", v.getId(), v.getCategorie(), v.getDescription(), v.getCouleur(), v.getSku()))
        );

        context.append("\n--- COMMANDES ---\n");
        commandeRepository.findAll().forEach(c -> 
            context.append(String.format("ID: %d | Client_ID: %d | Type: %s | Statut: %s | Prix: %.2f | Créée: %s | Livraison: %s\n", 
                c.getId(), c.getUtilisateurId(), c.getType(), c.getStatut(), c.getPrixTotal(), c.getDateCreation(), c.getDateLivraison()))
        );

        context.append("\nRéponds de manière concise et professionnelle aux questions de l'utilisateur en te basant UNIQUEMENT sur ces données. Si tu ne trouves pas l'information dans ces données, dis-le.");

        return geminiService.chat(message, context.toString());
    }
}
