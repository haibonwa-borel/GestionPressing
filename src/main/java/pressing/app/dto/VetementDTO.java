package pressing.app.dto;

import jakarta.validation.constraints.*;
import pressing.app.model.CategorieVetement;

import java.util.ArrayList;
import java.util.List;

/**
 * DTO pour le Vetement.
 */
public class VetementDTO {

    private Long id;
    private String sku;

    @NotBlank(message = "La description/nom est obligatoire")
    @Size(min = 3, max = 100, message = "La description doit faire entre 3 et 100 caracteres")
    private String description;

    @NotNull(message = "La categorie est obligatoire")
    private CategorieVetement categorie;

    @Size(max = 30, message = "La couleur ne doit pas depasser 30 caracteres")
    private String couleur;

    @Pattern(regexp = "^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$",
             message = "Le code couleur doit etre au format Hex (ex: #FFFFFF ou #FFF)")
    private String codeCouleur;

    private String photoUrl;
    private List<Long> commandeIds = new ArrayList<>();

    public VetementDTO() {}

    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public CategorieVetement getCategorie() { return categorie; }
    public void setCategorie(CategorieVetement categorie) { this.categorie = categorie; }

    public String getCouleur() { return couleur; }
    public void setCouleur(String couleur) { this.couleur = couleur; }

    public String getCodeCouleur() { return codeCouleur; }
    public void setCodeCouleur(String codeCouleur) { this.codeCouleur = codeCouleur; }

    public String getPhotoUrl() { return photoUrl; }
    public void setPhotoUrl(String photoUrl) { this.photoUrl = photoUrl; }

    public List<Long> getCommandeIds() { return commandeIds; }
    public void setCommandeIds(List<Long> commandeIds) { this.commandeIds = commandeIds; }
}
