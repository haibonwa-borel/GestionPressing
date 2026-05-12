package pressing.app.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import pressing.app.validation.TelephoneValide;

/**
 * DTO pour l'Utilisateur.
 * Utilise pour les entrees (formulaire) et les sorties (JSON).
 */
public class UtilisateurDTO {

    private Long id;

    @NotBlank(message = "Le nom est obligatoire")
    @Size(min = 2, max = 50, message = "Le nom doit faire entre 2 et 50 caracteres")
    private String nom;

    @NotBlank(message = "Le prenom est obligatoire")
    @Size(min = 2, max = 50, message = "Le prenom doit faire entre 2 et 50 caracteres")
    private String prenom;

    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "L'email doit etre valide")
    private String email;

    /**
     * "Decore" le mot de passe pour qu'il soit uniquement en ECRITURE.
     * Il ne sera JAMAIS renvoye dans la reponse JSON.
     */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @NotBlank(message = "Le mot de passe est obligatoire")
    @Size(min = 4, max = 100, message = "Le mot de passe doit faire entre 4 et 100 caracteres")
    private String motDePasse;

    @TelephoneValide
    private String telephone;

    /**
     * Role de l'utilisateur : ADMIN ou CLIENT.
     */
    private String role;

    public UtilisateurDTO() {}

    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getMotDePasse() { return motDePasse; }
    public void setMotDePasse(String motDePasse) { this.motDePasse = motDePasse; }

    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}
