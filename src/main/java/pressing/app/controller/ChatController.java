package pressing.app.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import pressing.app.repository.CommandeRepository;
import pressing.app.repository.UtilisateurRepository;
import pressing.app.repository.VetementRepository;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    @Value("${gemini.api.key:}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();
    
    private final UtilisateurRepository utilisateurRepository;
    private final CommandeRepository commandeRepository;
    private final VetementRepository vetementRepository;

    public ChatController(UtilisateurRepository utilisateurRepository,
                          CommandeRepository commandeRepository,
                          VetementRepository vetementRepository) {
        this.utilisateurRepository = utilisateurRepository;
        this.commandeRepository = commandeRepository;
        this.vetementRepository = vetementRepository;
    }

    @PostMapping
    public Map<String, Object> chat(@RequestBody Map<String, String> request) {
        String message = request.get("message");
        if (message == null || message.isEmpty()) {
            return Collections.singletonMap("error", "Message vide");
        }

        if (apiKey == null || apiKey.isEmpty()) {
            return Collections.singletonMap("error", "Clé API Gemini non configurée");
        }

        String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=" + apiKey;

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
                c.getId(), c.getUtilisateur() != null ? c.getUtilisateur().getId() : null, c.getType(), c.getStatut(), c.getPrixTotal(), c.getDateCreation(), c.getDateLivraison()))
        );

        context.append("\nRéponds de manière concise et professionnelle aux questions de l'utilisateur en te basant UNIQUEMENT sur ces données. Si tu ne trouves pas l'information dans ces données, dis-le.");

        // Structure Gemini API request
        Map<String, Object> geminiRequest = new HashMap<>();
        
        // System instruction
        Map<String, Object> sysContent = new HashMap<>();
        Map<String, String> sysPart = new HashMap<>();
        sysPart.put("text", context.toString());
        sysContent.put("parts", Collections.singletonList(sysPart));
        geminiRequest.put("system_instruction", sysContent);

        // User message
        Map<String, Object> content = new HashMap<>();
        Map<String, String> part = new HashMap<>();
        part.put("text", message);
        content.put("parts", Collections.singletonList(part));
        geminiRequest.put("contents", Collections.singletonList(content));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(geminiRequest, headers);

        try {
            return restTemplate.postForObject(url, entity, Map.class);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Erreur lors de l'appel à Gemini : " + e.getMessage());
            return error;
        }
    }
}
