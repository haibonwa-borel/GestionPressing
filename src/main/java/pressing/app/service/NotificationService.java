package pressing.app.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pressing.app.model.Commande;
import pressing.app.model.Utilisateur;
import pressing.app.model.Vetement;
import pressing.app.repository.CommandeRepository;
import pressing.app.repository.UtilisateurRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Service pour la gestion des notifications (Factures et Rappels).
 */
@Service
public class NotificationService {

    private final EmailService emailService;
    private final CommandeRepository commandeRepository;
    private final UtilisateurRepository utilisateurRepository;

    public NotificationService(EmailService emailService,
                               CommandeRepository commandeRepository,
                               UtilisateurRepository utilisateurRepository) {
        this.emailService = emailService;
        this.commandeRepository = commandeRepository;
        this.utilisateurRepository = utilisateurRepository;
    }

    /**
     * Genere et envoie la facture d'une commande par mail.
     */
    public void envoyerFacture(Long commandeId) {
        Commande commande = commandeRepository.findById(commandeId)
                .orElseThrow(() -> new RuntimeException("Commande non trouvée : " + commandeId));

        Utilisateur client = utilisateurRepository.findById(commande.getUtilisateurId())
                .orElseThrow(() -> new RuntimeException("Client non trouvé : " + commande.getUtilisateurId()));

        String subject = "🧾 Facture de votre commande #" + commande.getId();
        String html = genererHtmlFacture(commande, client);

        emailService.envoyerEmailHtml(client.getEmail(), subject, html);
    }

    /**
     * Envoie un rappel pour toutes les commandes a retirer dans moins de 24h.
     * Execute automatiquement toutes les 6 heures.
     */
    @Scheduled(cron = "0 0 */6 * * *")
    @Transactional(readOnly = true)
    public void envoyerRappelsAutomatiques() {
        int count = declencherRappelsManuels();
        System.out.println("⏰ Rappels automatiques envoyés : " + count);
    }

    /**
     * Declencher manuellement l'envoi des rappels (pour test API).
     * Retourne le nombre de rappels envoyés.
     */
    public int declencherRappelsManuels() {
        LocalDateTime maintenant = LocalDateTime.now();
        LocalDateTime dans24h = maintenant.plusHours(24);

        List<Commande> aRappeler = commandeRepository.findAll().stream()
                .filter(c -> c.getDateLivraison() != null
                        && c.getDateLivraison().isAfter(maintenant)
                        && c.getDateLivraison().isBefore(dans24h))
                .toList();

        for (Commande c : aRappeler) {
            envoyerMailRappel(c);
        }
        return aRappeler.size();
    }

    private void envoyerMailRappel(Commande commande) {
        Utilisateur client = utilisateurRepository.findById(commande.getUtilisateurId()).orElse(null);
        if (client == null) return;

        String subject = "⏰ Rappel : Vos vêtements sont prêts à être retirés !";
        String dateStr = commande.getDateLivraison().format(DateTimeFormatter.ofPattern("dd/MM/yyyy à HH:mm"));

        String html = String.format("""
            <div style="font-family:'Segoe UI',Tahoma,sans-serif; max-width:600px; margin:auto;
                        background:#f9f9f9; border-radius:12px; padding:30px; border:1px solid #e0e0e0;">
                <h2 style="color:#e67e22;">⏰ Rappel de retrait</h2>
                <p>Bonjour <b>%s</b>,</p>
                <p>Votre commande <b>#%d</b> est prête et doit être retirée le :</p>
                <div style="text-align:center; padding:15px; background:#fff; border-radius:8px;
                            border:2px solid #e67e22; font-size:1.4em; font-weight:bold; color:#e67e22;">
                    📅 %s
                </div>
                <p style="margin-top:20px;">Merci de nous rendre visite dans les meilleurs délais.</p>
                <hr style="border:none; border-top:1px solid #ddd; margin:20px 0;">
                <p style="color:#888; font-size:0.85em;">L'équipe de votre Pressing</p>
            </div>
            """, client.getPrenom(), commande.getId(), dateStr);

        emailService.envoyerEmailHtml(client.getEmail(), subject, html);
    }

    private String genererHtmlFacture(Commande c, Utilisateur u) {
        StringBuilder itemsHtml = new StringBuilder();
        if (c.getVetements() != null) {
            for (Vetement v : c.getVetements()) {
                itemsHtml.append(String.format(
                        "<tr><td style='padding:8px;border-bottom:1px solid #eee;'>%s</td>" +
                        "<td style='padding:8px;border-bottom:1px solid #eee;color:#7f8c8d;'>%s</td>" +
                        "<td style='padding:8px;border-bottom:1px solid #eee;'>%s</td></tr>",
                        v.getDescription(),
                        v.getCategorie(),
                        v.getCouleur() != null ? v.getCouleur() : "-"));
            }
        }

        String dateC = c.getDateCreation() != null
                ? c.getDateCreation().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "-";
        String dateL = c.getDateLivraison() != null
                ? c.getDateLivraison().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "-";

        return String.format("""
            <div style="font-family:'Segoe UI',Tahoma,sans-serif; max-width:640px; margin:auto;
                        background:#ffffff; border-radius:12px; overflow:hidden;
                        box-shadow:0 4px 20px rgba(0,0,0,0.08);">
                
                <!-- En-tête -->
                <div style="background:linear-gradient(135deg,#2c3e50,#3498db);
                            padding:30px; text-align:center; color:white;">
                    <h1 style="margin:0; font-size:1.8em;">🧺 Pressing App</h1>
                    <p style="margin:5px 0 0; opacity:0.85;">Facture #%d</p>
                </div>
                
                <!-- Corps -->
                <div style="padding:30px;">
                    <p>Bonjour <b>%s %s</b>,</p>
                    <p>Merci pour votre commande. Voici votre facture :</p>
                    
                    <!-- Infos commande -->
                    <table style="width:100%%; border-collapse:collapse; margin:20px 0;
                                  background:#f8f9fa; border-radius:8px; overflow:hidden;">
                        <tr>
                            <td style="padding:10px 15px;"><b>📅 Date de dépôt</b></td>
                            <td style="padding:10px 15px;">%s</td>
                        </tr>
                        <tr style="background:#fff;">
                            <td style="padding:10px 15px;"><b>🚀 Type de service</b></td>
                            <td style="padding:10px 15px;">%s</td>
                        </tr>
                        <tr>
                            <td style="padding:10px 15px;"><b>📦 Date de retrait prévue</b></td>
                            <td style="padding:10px 15px; color:#27ae60; font-weight:bold;">%s</td>
                        </tr>
                    </table>
                    
                    <!-- Articles -->
                    <h3 style="color:#2c3e50;">🧥 Articles :</h3>
                    <table style="width:100%%; border-collapse:collapse;">
                        <thead>
                            <tr style="background:#3498db; color:white;">
                                <th style="padding:10px; text-align:left;">Description</th>
                                <th style="padding:10px; text-align:left;">Catégorie</th>
                                <th style="padding:10px; text-align:left;">Couleur</th>
                            </tr>
                        </thead>
                        <tbody>%s</tbody>
                    </table>
                    
                    <!-- Total -->
                    <div style="text-align:right; margin-top:20px; padding:15px;
                                background:#f8f9fa; border-radius:8px;">
                        <span style="font-size:1.4em; font-weight:bold; color:#e74c3c;">
                            Total : %.0f FCFA
                        </span>
                    </div>
                </div>
                
                <!-- Pied de page -->
                <div style="background:#2c3e50; color:#aaa; text-align:center; padding:15px; font-size:0.85em;">
                    Pressing App | noreply@pressing-app.com
                </div>
            </div>
            """,
                c.getId(), u.getPrenom(), u.getNom(),
                dateC, c.getType(), dateL,
                itemsHtml.toString(), c.getPrixTotal());
    }
}
