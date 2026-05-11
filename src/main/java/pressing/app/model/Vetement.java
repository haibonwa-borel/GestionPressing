package pressing.app.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Entite JPA Vetement.
 * Mapped vers la table "vetements".
 * Cote inverse de la relation ManyToMany avec Commande.
 */
@Entity
@Table(name = "vetements")
public class Vetement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, length = 20)
    private String sku;

    @Column(nullable = false, length = 100)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CategorieVetement categorie;

    @Column(length = 30)
    private String couleur;

    @Column(name = "code_couleur", length = 10)
    private String codeCouleur;

    @Column(name = "photo_url")
    private String photoUrl;

    /**
     * Cote inverse de la relation ManyToMany.
     * "mappedBy" indique que Commande est le proprietaire de la relation.
     */
    @ManyToMany(mappedBy = "vetements")
    private List<Commande> commandes = new ArrayList<>();

    public Vetement() {}

    public static String genererSku() {
        return "VET-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    // --- Getters/Setters ---
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

    public List<Commande> getCommandes() { return commandes; }
    public void setCommandes(List<Commande> commandes) { this.commandes = commandes; }
}
