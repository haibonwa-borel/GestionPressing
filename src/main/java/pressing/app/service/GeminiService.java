package pressing.app.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class GeminiService {

    @Value("${gemini.api.key:}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Analyse une image pour en extraire la catégorie de vêtement et la couleur.
     */
    public Map<String, String> analyserImage(byte[] imageBytes) {
        if (apiKey == null || apiKey.isEmpty()) {
            return Collections.emptyMap();
        }

        String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=" + apiKey;

        String prompt = "Analyse cette image de vêtement. Retourne UNIQUEMENT un objet JSON avec les clés suivantes : " +
                "\"categorie\" (choisis parmi: CHEMISE, PANTALON, ROBE, VESTE, MANTEAU, JUPE, TSHIRT, COSTUME, AUTRE) " +
                "et \"couleur\" (nom de la couleur principale en français).";

        String base64Image = Base64.getEncoder().encodeToString(imageBytes);

        Map<String, Object> partText = new HashMap<>();
        partText.put("text", prompt);

        Map<String, Object> partImage = new HashMap<>();
        Map<String, String> inlineData = new HashMap<>();
        inlineData.put("mime_type", "image/jpeg");
        inlineData.put("data", base64Image);
        partImage.put("inline_data", inlineData);

        Map<String, Object> content = new HashMap<>();
        content.put("parts", Arrays.asList(partText, partImage));

        Map<String, Object> geminiRequest = new HashMap<>();
        geminiRequest.put("contents", Collections.singletonList(content));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(geminiRequest, headers);

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.postForObject(url, entity, Map.class);
            return extraireResultatJson(response);
        } catch (Exception e) {
            System.err.println("Erreur Gemini Image Analysis: " + e.getMessage());
            return Collections.emptyMap();
        }
    }

    /**
     * Assistant Chatbot.
     */
    public Map<String, Object> chat(String message, String systemContext) {
        if (apiKey == null || apiKey.isEmpty()) {
            return Collections.singletonMap("error", "Clé API Gemini non configurée");
        }

        String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=" + apiKey;

        Map<String, Object> geminiRequest = new HashMap<>();
        
        Map<String, Object> sysContent = new HashMap<>();
        Map<String, String> sysPart = new HashMap<>();
        sysPart.put("text", systemContext);
        sysContent.put("parts", Collections.singletonList(sysPart));
        geminiRequest.put("system_instruction", sysContent);

        Map<String, Object> content = new HashMap<>();
        Map<String, String> part = new HashMap<>();
        part.put("text", message);
        content.put("parts", Collections.singletonList(part));
        geminiRequest.put("contents", Collections.singletonList(content));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(geminiRequest, headers);

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.postForObject(url, entity, Map.class);
            return response;
        } catch (Exception e) {
            return Collections.singletonMap("error", "Erreur lors de l'appel à Gemini : " + e.getMessage());
        }
    }

    private Map<String, String> extraireResultatJson(Map<String, Object> response) {
        try {
            List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.get("candidates");
            Map<String, Object> content = (Map<String, Object>) candidates.get(0).get("content");
            List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
            String text = (String) parts.get(0).get("text");

            text = text.replace("```json", "").replace("```", "").trim();
            
            Map<String, String> result = new HashMap<>();
            // Extraction rudimentaire de la catégorie
            for (String cat : Arrays.asList("CHEMISE", "PANTALON", "ROBE", "VESTE", "MANTEAU", "JUPE", "TSHIRT", "COSTUME")) {
                if (text.toUpperCase().contains(cat)) {
                    result.put("categorie", cat);
                    break;
                }
            }
            if (!result.containsKey("categorie")) result.put("categorie", "AUTRE");

            // Extraction rudimentaire de la couleur
            for (String col : Arrays.asList("bleu", "rouge", "vert", "noir", "blanc", "gris", "jaune", "rose", "marron")) {
                if (text.toLowerCase().contains(col)) {
                    result.put("couleur", col);
                    break;
                }
            }
            return result;
        } catch (Exception e) {
            return Collections.emptyMap();
        }
    }
}
