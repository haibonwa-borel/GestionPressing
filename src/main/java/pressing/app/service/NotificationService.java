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
     * Genere et envoie la facture d'une commande par mail avec la pièce jointe PDF.
     */
    @Transactional(readOnly = true)
    public void envoyerFacture(Long commandeId) {
        Commande commande = commandeRepository.findById(commandeId)
                .orElseThrow(() -> new RuntimeException("Commande non trouvée : " + commandeId));

        Utilisateur client = utilisateurRepository.findById(commande.getUtilisateurId())
                .orElseThrow(() -> new RuntimeException("Client non trouvé : " + commande.getUtilisateurId()));

        String subject = "🧾 Facture de votre commande #" + commande.getId();
        String html = genererHtmlFacture(commande, client);
        
        // Wrap for PDF generation (XHTML strict)
        String xhtml = "<!DOCTYPE html><html><head><meta charset=\"UTF-8\"/></head><body>" + html + "</body></html>";
        byte[] pdfBytes = null;
        try {
            pdfBytes = genererPdfAPartirDeHtml(xhtml);
        } catch (Exception e) {
            System.err.println("❌ Erreur de génération du PDF : " + e.getMessage());
        }

        if (pdfBytes != null) {
            emailService.envoyerEmailHtmlAvecPieceJointe(client.getEmail(), subject, html, pdfBytes, "Facture_Commande_" + commande.getId() + ".pdf");
        } else {
            emailService.envoyerEmailHtml(client.getEmail(), subject, html);
        }
    }

    private byte[] genererPdfAPartirDeHtml(String xhtml) throws Exception {
        try (java.io.ByteArrayOutputStream os = new java.io.ByteArrayOutputStream()) {
            com.openhtmltopdf.pdfboxout.PdfRendererBuilder builder = new com.openhtmltopdf.pdfboxout.PdfRendererBuilder();
            builder.useFastMode();
            builder.withHtmlContent(xhtml, "http://localhost:8080/");
            builder.toStream(os);
            builder.run();
            return os.toByteArray();
        }
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
                .filter(c -> c.getDateLivraison() != null)
                .filter(c -> c.getDateLivraison().isAfter(maintenant) && c.getDateLivraison().isBefore(dans24h))
                .filter(c -> c.getStatut() == pressing.app.model.StatutCommande.TERMINEE)
                .toList();

        for (Commande c : aRappeler) {
            envoyerRappel(c);
        }
        return aRappeler.size();
    }

    private void envoyerRappel(Commande commande) {
        Utilisateur client = utilisateurRepository.findById(commande.getUtilisateurId()).orElse(null);
        if (client == null || client.getEmail() == null) return;

        String subject = "⏰ Rappel : Votre commande est prête !";
        String dateStr = commande.getDateLivraison() != null
                ? commande.getDateLivraison().format(DateTimeFormatter.ofPattern("dd/MM/yyyy à HH:mm"))
                : "bientôt";

        String html = String.format("""
            <div style="font-family:'Segoe UI',Tahoma,sans-serif; max-width:600px; margin:auto;
                        background:#212121; color:#f1f1f1; border-radius:12px; padding:30px; border:1px solid #3f3f3f;">
                <h2 style="color:#3ea6ff;">⏰ Rappel de retrait</h2>
                <p>Bonjour <b>%s</b>,</p>
                <p>Votre commande <b>#%d</b> est prête et doit être retirée le :</p>
                <div style="text-align:center; padding:15px; background:#0f0f0f; border-radius:8px;
                            border:1px solid #3ea6ff; font-size:1.4em; font-weight:bold; color:#3ea6ff;">
                    📅 %s
                </div>
                <p style="margin-top:20px;">Merci de nous rendre visite dans les meilleurs délais.</p>
                <hr style="border:none; border-top:1px solid #3f3f3f; margin:20px 0;"/>
                <p style="color:#aaaaaa; font-size:0.85em;">L'équipe de votre Pressing</p>
            </div>
            """, client.getPrenom(), commande.getId(), dateStr);

        emailService.envoyerEmailHtml(client.getEmail(), subject, html);
    }

    private String genererHtmlFacture(Commande c, Utilisateur u) {
        StringBuilder itemsHtml = new StringBuilder();
        if (c.getVetements() != null) {
            for (Vetement v : c.getVetements()) {
                String photoTag = (v.getPhotoUrl() != null && !v.getPhotoUrl().isEmpty()) 
                    ? "<img src='http://localhost:8080" + v.getPhotoUrl() + "' style='width:40px;height:40px;object-fit:cover;border-radius:4px;' alt='photo'/>" 
                    : "-";
                    
                itemsHtml.append(String.format(
                        "<tr><td style='padding:8px;border-bottom:1px solid #3f3f3f;text-align:center;'>%s</td>" +
                        "<td style='padding:8px;border-bottom:1px solid #3f3f3f;color:#f1f1f1;'>%s</td>" +
                        "<td style='padding:8px;border-bottom:1px solid #3f3f3f;color:#aaaaaa;'>%s</td>" +
                        "<td style='padding:8px;border-bottom:1px solid #3f3f3f;color:#f1f1f1;'>%s</td></tr>",
                        photoTag,
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
                        background:#212121; color:#f1f1f1; border-radius:12px; overflow:hidden;
                        box-shadow:0 4px 20px rgba(0,0,0,0.4); border:1px solid #3f3f3f;">
                
                <!-- En-tête -->
                <div style="background:#0f0f0f; border-bottom:1px solid #3f3f3f;
                            padding:30px; text-align:center; color:#f1f1f1;">
                    <h1 style="margin:0; font-size:1.8em; color:#3ea6ff;">🧺 Pressing App</h1>
                    <p style="margin:5px 0 0; color:#aaaaaa;">Facture #%d</p>
                </div>
                
                <!-- Corps -->
                <div style="padding:30px;">
                    <p>Bonjour <b style="color:#3ea6ff;">%s %s</b>,</p>
                    <p>Merci pour votre commande. Voici votre facture :</p>
                    
                    <!-- Infos commande -->
                    <table style="width:100%%; border-collapse:collapse; margin:20px 0;
                                  background:#0f0f0f; border-radius:8px; overflow:hidden; border:1px solid #3f3f3f;">
                        <tr>
                            <td style="padding:10px 15px; border-bottom:1px solid #3f3f3f;"><b>📅 Date de dépôt</b></td>
                            <td style="padding:10px 15px; border-bottom:1px solid #3f3f3f; color:#aaaaaa;">%s</td>
                        </tr>
                        <tr>
                            <td style="padding:10px 15px; border-bottom:1px solid #3f3f3f;"><b>🚀 Type de service</b></td>
                            <td style="padding:10px 15px; border-bottom:1px solid #3f3f3f; color:#aaaaaa;">%s</td>
                        </tr>
                        <tr>
                            <td style="padding:10px 15px;"><b>📦 Date de retrait prévue</b></td>
                            <td style="padding:10px 15px; color:#2ba640; font-weight:bold;">%s</td>
                        </tr>
                    </table>
                    
                    <!-- Articles -->
                    <h3 style="color:#3ea6ff;">🧥 Articles :</h3>
                    <table style="width:100%%; border-collapse:collapse; margin-bottom:20px;">
                        <thead>
                            <tr style="background:#0f0f0f; color:#aaaaaa;">
                                <th style="padding:10px; text-align:center; border-bottom:1px solid #3f3f3f;">Photo</th>
                                <th style="padding:10px; text-align:left; border-bottom:1px solid #3f3f3f;">Description</th>
                                <th style="padding:10px; text-align:left; border-bottom:1px solid #3f3f3f;">Catégorie</th>
                                <th style="padding:10px; text-align:left; border-bottom:1px solid #3f3f3f;">Couleur</th>
                            </tr>
                        </thead>
                        <tbody>%s</tbody>
                    </table>
                    
                    <!-- Total -->
                    <div style="text-align:right; margin-top:20px; padding:15px;
                                background:#0f0f0f; border-radius:8px; border:1px solid #3f3f3f;">
                        <span style="font-size:1.4em; font-weight:bold; color:#ff4e4e;">
                            Total : %.0f FCFA
                        </span>
                    </div>
                </div>
                
                <!-- Pied de page -->
                <div style="background:#0f0f0f; color:#aaaaaa; text-align:center; padding:15px; font-size:0.85em; border-top:1px solid #3f3f3f;">
                    Pressing App | noreply@pressing-app.com
                </div>
            </div>
            """,
                c.getId(), u.getPrenom(), u.getNom(),
                dateC, c.getType(), dateL,
                itemsHtml.toString(), c.getPrixTotal());
    }
}
