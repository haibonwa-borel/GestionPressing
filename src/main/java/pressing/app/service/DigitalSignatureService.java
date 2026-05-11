package pressing.app.service;

import jakarta.annotation.PostConstruct;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.PDSignature;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.SignatureInterface;
import org.bouncycastle.cert.jcajce.JcaCertStore;
import org.bouncycastle.asn1.cms.CMSObjectIdentifiers;
import org.bouncycastle.cms.*;
import org.bouncycastle.cms.jcajce.JcaSignerInfoGeneratorBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.operator.jcajce.JcaDigestCalculatorProviderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.*;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Service de signature numerique des documents PDF.
 * Utilise un keystore PKCS12 et Bouncy Castle pour signer les PDF
 * avec une signature PKCS7 detachee (standard PDF).
 *
 * Touche perso : ajoute un bandeau de certification visible sur la derniere page du PDF
 * avec un hash SHA-256 unique pour verifier l'authenticite du document.
 */
@Service
public class DigitalSignatureService {

    @Value("${pressing.signature.keystore-path}")
    private Resource keystoreResource;

    @Value("${pressing.signature.keystore-password}")
    private String keystorePassword;

    @Value("${pressing.signature.key-alias}")
    private String keyAlias;

    @Value("${pressing.signature.raison:Document officiel}")
    private String raison;

    @Value("${pressing.signature.lieu:Douala, Cameroun}")
    private String lieu;

    @Value("${pressing.signature.contact:}")
    private String contact;

    private PrivateKey privateKey;
    private Certificate[] certificateChain;

    @PostConstruct
    public void init() {
        try {
            Security.addProvider(new BouncyCastleProvider());

            KeyStore keyStore = KeyStore.getInstance("PKCS12");
            try (InputStream is = keystoreResource.getInputStream()) {
                keyStore.load(is, keystorePassword.toCharArray());
            }

            this.privateKey = (PrivateKey) keyStore.getKey(keyAlias, keystorePassword.toCharArray());
            this.certificateChain = keyStore.getCertificateChain(keyAlias);

            if (this.privateKey == null || this.certificateChain == null) {
                throw new RuntimeException("Cle privee ou chaine de certificats introuvable pour l'alias : " + keyAlias);
            }

            System.out.println("[DigitalSignatureService] Keystore charge avec succes - Certificat : "
                    + ((X509Certificate) certificateChain[0]).getSubjectX500Principal().getName());

        } catch (Exception e) {
            throw new RuntimeException("Impossible de charger le keystore de signature : " + e.getMessage(), e);
        }
    }

    /**
     * Signe un PDF et ajoute un bandeau de certification visible.
     * 
     * @param pdfBytes le PDF non signe
     * @return le PDF signe avec bandeau de certification
     */
    public byte[] signerPdf(byte[] pdfBytes, String referenceFacture) {
        try {
            System.out.println("[DigitalSignatureService] Debut signature PDF - Thread: " + Thread.currentThread().getName());

            // Etape 1 : Calculer le hash SHA-256 du PDF original pour le bandeau
            String hashDocument = calculerHashSHA256(pdfBytes);

            // Etape 2 : Ajouter le bandeau de certification visible sur la derniere page
            byte[] pdfAvecBandeau = ajouterBandeauCertification(pdfBytes, hashDocument, referenceFacture);

            // Etape 3 : Signer numériquement le PDF
            byte[] pdfSigne = appliquerSignatureNumerique(pdfAvecBandeau);

            System.out.println("[DigitalSignatureService] PDF signe avec succes (" + pdfSigne.length + " octets)");
            return pdfSigne;

        } catch (Exception e) {
            System.err.println("[DigitalSignatureService] Erreur lors de la signature : " + e.getMessage());
            e.printStackTrace();
            // En cas d'erreur de signature, retourner le PDF original non signe
            return pdfBytes;
        }
    }

    /**
     * Ajoute un bandeau de certification elegant en bas de la derniere page du PDF.
     * C'est la "touche perso" : un tampon visuel avec hash de verification.
     */
    private byte[] ajouterBandeauCertification(byte[] pdfBytes, String hashDocument, String referenceFacture) throws IOException {
        try (PDDocument document = PDDocument.load(pdfBytes)) {
            PDPage lastPage = document.getPage(document.getNumberOfPages() - 1);
            PDRectangle mediaBox = lastPage.getMediaBox();

            try (PDPageContentStream cs = new PDPageContentStream(document, lastPage, PDPageContentStream.AppendMode.APPEND, true, true)) {

                float bandeauY = 20;
                float bandeauX = 30;
                float bandeauWidth = mediaBox.getWidth() - 60;
                float bandeauHeight = 55;

                // Fond du bandeau (gris fonce semi-transparent)
                cs.setNonStrokingColor(0.12f, 0.12f, 0.15f);
                cs.addRect(bandeauX, bandeauY, bandeauWidth, bandeauHeight);
                cs.fill();

                // Bordure accent (bleu pressing)
                cs.setStrokingColor(0.24f, 0.65f, 1.0f); // #3EA6FF
                cs.setLineWidth(1.5f);
                cs.addRect(bandeauX, bandeauY, bandeauWidth, bandeauHeight);
                cs.stroke();

                // Ligne d'accent en haut du bandeau
                cs.setStrokingColor(0.24f, 0.65f, 1.0f);
                cs.setLineWidth(2f);
                cs.moveTo(bandeauX, bandeauY + bandeauHeight);
                cs.lineTo(bandeauX + bandeauWidth, bandeauY + bandeauHeight);
                cs.stroke();

                // Texte principal
                PDType1Font fontBold = PDType1Font.HELVETICA_BOLD;
                PDType1Font fontNormal = PDType1Font.HELVETICA;

                // Icone + "DOCUMENT CERTIFIE"
                cs.beginText();
                cs.setFont(fontBold, 8);
                cs.setNonStrokingColor(0.24f, 0.65f, 1.0f);
                cs.newLineAtOffset(bandeauX + 10, bandeauY + bandeauHeight - 15);
                cs.showText("\u2713 DOCUMENT SIGNE NUMERIQUEMENT - PRESSING APP");
                cs.endText();

                // Date et lieu
                String dateSignature = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy a HH:mm:ss"));
                cs.beginText();
                cs.setFont(fontNormal, 6.5f);
                cs.setNonStrokingColor(0.7f, 0.7f, 0.7f);
                cs.newLineAtOffset(bandeauX + 10, bandeauY + bandeauHeight - 27);
                cs.showText("Signe le " + dateSignature + " | " + lieu + " | Réf: " + referenceFacture);
                cs.endText();

                // Hash SHA-256 (empreinte unique du document)
                cs.beginText();
                cs.setFont(fontNormal, 5.5f);
                cs.setNonStrokingColor(0.5f, 0.5f, 0.5f);
                cs.newLineAtOffset(bandeauX + 10, bandeauY + bandeauHeight - 38);
                cs.showText("Empreinte SHA-256 : " + hashDocument);
                cs.endText();

                // Contact
                cs.beginText();
                cs.setFont(fontNormal, 5.5f);
                cs.setNonStrokingColor(0.5f, 0.5f, 0.5f);
                cs.newLineAtOffset(bandeauX + 10, bandeauY + bandeauHeight - 48);
                cs.showText("Certificat : CN=Pressing App, O=Pressing | Contact : " + contact);
                cs.endText();
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            document.save(baos);
            return baos.toByteArray();
        }
    }

    /**
     * Applique la signature numerique PKCS7 detachee au PDF.
     */
    private byte[] appliquerSignatureNumerique(byte[] pdfBytes) throws Exception {
        // On cree le SignatureInterface pour Bouncy Castle
        SignatureInterface signatureInterface = content -> {
            try {
                CMSSignedDataGenerator gen = new CMSSignedDataGenerator();

                X509Certificate cert = (X509Certificate) certificateChain[0];
                ContentSigner sha256Signer = new JcaContentSignerBuilder("SHA256WithRSA")
                        .setProvider("BC")
                        .build(privateKey);

                gen.addSignerInfoGenerator(
                        new JcaSignerInfoGeneratorBuilder(
                                new JcaDigestCalculatorProviderBuilder().setProvider("BC").build()
                        ).build(sha256Signer, cert)
                );

                gen.addCertificates(new JcaCertStore(Arrays.asList(certificateChain)));

                CMSProcessableInputStream msg = new CMSProcessableInputStream(content);
                CMSSignedData signedData = gen.generate(msg, false);

                return signedData.getEncoded();
            } catch (Exception e) {
                throw new IOException("Erreur lors de la signature PKCS7 : " + e.getMessage(), e);
            }
        };

        // Charger le PDF, ajouter la signature et sauvegarder
        try (PDDocument document = PDDocument.load(pdfBytes)) {
            PDSignature signature = new PDSignature();
            signature.setFilter(PDSignature.FILTER_ADOBE_PPKLITE);
            signature.setSubFilter(PDSignature.SUBFILTER_ADBE_PKCS7_DETACHED);
            signature.setName("Pressing App");
            signature.setLocation(lieu);
            signature.setReason(raison);
            signature.setContactInfo(contact);
            signature.setSignDate(Calendar.getInstance());

            document.addSignature(signature, signatureInterface);

            ByteArrayOutputStream signedOutputStream = new ByteArrayOutputStream();
            document.saveIncremental(signedOutputStream);
            return signedOutputStream.toByteArray();
        }
    }

    /**
     * Calcule le hash SHA-256 d'un contenu binaire (pour le bandeau de verification).
     */
    private String calculerHashSHA256(byte[] data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(data);
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            return "N/A";
        }
    }

    /**
     * Classe interne pour fournir le contenu a signer au CMSSignedDataGenerator.
     */
    private static class CMSProcessableInputStream implements CMSTypedData {
        private final InputStream inputStream;

        CMSProcessableInputStream(InputStream is) {
            this.inputStream = is;
        }

        @Override
        public Object getContent() {
            return inputStream;
        }

        @Override
        public void write(OutputStream out) throws IOException {
            byte[] buffer = new byte[8192];
            int read;
            while ((read = inputStream.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
        }

        @Override
        public org.bouncycastle.asn1.ASN1ObjectIdentifier getContentType() {
            return CMSObjectIdentifiers.data;
        }
    }
}
