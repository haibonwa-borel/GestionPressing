package pressing.app.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

/**
 * Service technique pour l'envoi d'emails HTML via JavaMailSender.
 * Supporte l'envoi synchrone et asynchrone.
 */
@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void envoyerEmailHtml(String to, String subject, String htmlContent) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            helper.setFrom("Contact@zevaba.com");

            mailSender.send(message);
            System.out.println("Email envoyé avec succès à : " + to);
        } catch (MessagingException e) {
            System.err.println("Erreur lors de l'envoi de l'email : " + e.getMessage());
            throw new RuntimeException("Erreur envoi email : " + e.getMessage(), e);
        }
    }

    public void envoyerEmailHtmlAvecPieceJointe(String to, String subject, String htmlContent, byte[] attachment, String attachmentName) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            helper.setFrom("Contact@zevaba.com");

            if (attachment != null && attachment.length > 0) {
                org.springframework.core.io.ByteArrayResource resource = new org.springframework.core.io.ByteArrayResource(attachment);
                helper.addAttachment(attachmentName, resource);
            }

            mailSender.send(message);
            System.out.println("Email avec pièce jointe envoyé avec succès à : " + to);
        } catch (MessagingException e) {
            System.err.println("Erreur lors de l'envoi de l'email avec PJ : " + e.getMessage());
            throw new RuntimeException("Erreur envoi email avec PJ : " + e.getMessage(), e);
        }
    }

    /**
     * Envoi d'email HTML de maniere asynchrone.
     */
    @Async
    public CompletableFuture<Void> envoyerEmailHtmlAsync(String to, String subject, String htmlContent) {
        System.out.println("[EmailService] Envoi async email - Thread: " + Thread.currentThread().getName());
        envoyerEmailHtml(to, subject, htmlContent);
        return CompletableFuture.completedFuture(null);
    }

    /**
     * Envoi d'email HTML avec piece jointe de maniere asynchrone.
     */
    @Async
    public CompletableFuture<Void> envoyerEmailHtmlAvecPieceJointeAsync(String to, String subject, String htmlContent, byte[] attachment, String attachmentName) {
        System.out.println("[EmailService] Envoi async email avec PJ - Thread: " + Thread.currentThread().getName());
        envoyerEmailHtmlAvecPieceJointe(to, subject, htmlContent, attachment, attachmentName);
        return CompletableFuture.completedFuture(null);
    }
}
