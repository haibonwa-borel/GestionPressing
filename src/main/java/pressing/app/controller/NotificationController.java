package pressing.app.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pressing.app.service.EmailService;
import pressing.app.service.NotificationService;

/**
 * Controller pour declencher manuellement les notifications.
 */
@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;
    private final EmailService emailService;

    public NotificationController(NotificationService notificationService, EmailService emailService) {
        this.notificationService = notificationService;
        this.emailService = emailService;
    }

    /**
     * Envoyer un mail de test a une adresse donnée.
     * Exemple : POST /api/notifications/test?to=haibonwaborel@gmail.com
     */
    @PostMapping("/test")
    public ResponseEntity<String> envoyerMailTest(@RequestParam String to) {
        try {
            String html = """
                <div style="font-family: 'Segoe UI', Tahoma, sans-serif; max-width: 600px; margin: auto;
                            background: #f9f9f9; border-radius: 12px; padding: 30px; border: 1px solid #e0e0e0;">
                    <h1 style="color: #3498db; text-align: center;"><i class="fas fa-check-circle"></i> Test Email Pressing App</h1>
                    <p style="font-size: 1.1em; color: #2c3e50;">Bonjour,</p>
                    <p>Ce mail confirme que la configuration email de votre application Spring Boot 
                       <b>Gestion de Pressing</b> fonctionne correctement.</p>
                    <hr style="border: none; border-top: 1px solid #ddd; margin: 20px 0;">
                    <p style="color: #7f8c8d; font-size: 0.9em;">
                        📧 Envoyé depuis <b>Pressing App</b> | noreply@pressing-app.com
                    </p>
                </div>
                """;
            emailService.envoyerEmailHtml(to, "Test - Configuration Email OK", html);
            return ResponseEntity.ok("Mail de test envoyé avec succès à : " + to);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Erreur : " + e.getMessage());
        }
    }

    /**
     * Envoyer manuellement la facture d'une commande par mail.
     */
    @PostMapping("/commandes/{commandeId}/facture")
    public ResponseEntity<String> envoyerFactureManuelle(@PathVariable Long commandeId) {
        try {
            notificationService.envoyerFacture(commandeId);
            return ResponseEntity.ok("Facture envoyée avec succès par mail.");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Erreur lors de l'envoi : " + e.getMessage());
        }
    }

    /**
     * Declencher manuellement la verification des rappels (moins de 24h).
     */
    @PostMapping("/rappels/declencher")
    public ResponseEntity<String> declencherRappels() {
        try {
            int nbEnvoyes = notificationService.declencherRappelsManuels();
            return ResponseEntity.ok("Rappels envoyés : " + nbEnvoyes);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Erreur lors du déclenchement : " + e.getMessage());
        }
    }
}
