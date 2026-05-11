package pressing.app.service;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.concurrent.CompletableFuture;

/**
 * Service dedie a la generation de PDF a partir de contenu HTML.
 * La generation est effectuee de maniere asynchrone.
 * Chaque PDF genere est automatiquement signe numeriquement.
 */
@Service
public class PdfService {

    private final DigitalSignatureService signatureService;

    public PdfService(DigitalSignatureService signatureService) {
        this.signatureService = signatureService;
    }

    /**
     * Genere un PDF signe a partir d'un contenu HTML de maniere asynchrone.
     * Retourne un CompletableFuture contenant les bytes du PDF signe.
     */
    @Async
    public CompletableFuture<byte[]> genererPdfAsync(String htmlContent, String referenceFacture) {
        System.out.println("[PdfService] Debut generation PDF - Thread: " + Thread.currentThread().getName());
        try {
            String xhtml = "<!DOCTYPE html><html><head><meta charset=\"UTF-8\"/></head><body>" + htmlContent + "</body></html>";
            byte[] pdfBytes = genererPdfAPartirDeHtml(xhtml);
            System.out.println("[PdfService] PDF genere (" + pdfBytes.length + " octets), lancement signature...");

            // Signer le PDF
            byte[] pdfSigne = signatureService.signerPdf(pdfBytes, referenceFacture);
            System.out.println("[PdfService] PDF signe avec succes (" + pdfSigne.length + " octets)");

            return CompletableFuture.completedFuture(pdfSigne);
        } catch (Exception e) {
            System.err.println("[PdfService] Erreur de generation du PDF : " + e.getMessage());
            return CompletableFuture.completedFuture(null);
        }
    }

    /**
     * Genere un PDF signe de maniere synchrone.
     */
    public byte[] genererPdfSync(String htmlContent, String referenceFacture) {
        try {
            String xhtml = "<!DOCTYPE html><html><head><meta charset=\"UTF-8\"/></head><body>" + htmlContent + "</body></html>";
            byte[] pdfBytes = genererPdfAPartirDeHtml(xhtml);
            return signatureService.signerPdf(pdfBytes, referenceFacture);
        } catch (Exception e) {
            System.err.println("[PdfService] Erreur de generation du PDF : " + e.getMessage());
            return null;
        }
    }

    private byte[] genererPdfAPartirDeHtml(String xhtml) throws Exception {
        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            com.openhtmltopdf.pdfboxout.PdfRendererBuilder builder = new com.openhtmltopdf.pdfboxout.PdfRendererBuilder();
            builder.useFastMode();
            builder.withHtmlContent(xhtml, "http://localhost:8080/");
            builder.toStream(os);
            builder.run();
            return os.toByteArray();
        }
    }
}
