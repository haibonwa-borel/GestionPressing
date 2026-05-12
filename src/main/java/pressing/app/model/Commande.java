package pressing.app.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entite JPA Commande.
 * Mapped vers la table "commandes".
 * Proprietaire de la relation ManyToMany avec Vetement.
 */
@Entity
@Table(name = "commandes")
public class Commande {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private TypeCommande type;

    @Enumerated(EnumType.STRING)
    @Column(length = 15)
    private StatutCommande statut;

    @Column(name = "date_creation")
    private LocalDateTime dateCreation;

    @Column(name = "date_livraison")
    private LocalDateTime dateLivraison;

    @Column(name = "prix_total")
    private double prixTotal;

    @Column(name = "utilisateur_id", nullable = false)
    private Long utilisateurId;

    @Column(name = "facture_url")
    private String factureUrl;

    /**
     * Proprietaire de la relation ManyToMany.
     * La table de jointure "commande_vetement" est geree automatiquement par JPA.
     * 
     * CascadeType.PERSIST/MERGE : Persiste/met a jour les vetements avec la commande.
     * orphanRemoval N'EST PAS utilise ici car un vetement peut appartenir a plusieurs commandes.
     */
    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
        name = "commande_vetement",
        joinColumns = @JoinColumn(name = "commande_id"),
        inverseJoinColumns = @JoinColumn(name = "vetement_id")
    )
    private List<Vetement> vetements = new ArrayList<>();

    public Commande() {}

    public Commande(Long id, TypeCommande type, StatutCommande statut,
                    LocalDateTime dateCreation, LocalDateTime dateLivraison,
                    double prixTotal, Long utilisateurId) {
        this.id = id;
        this.type = type;
        this.statut = statut;
        this.dateCreation = dateCreation;
        this.dateLivraison = dateLivraison;
        this.prixTotal = prixTotal;
        this.utilisateurId = utilisateurId;
    }

    // --- Getters/Setters ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public TypeCommande getType() { return type; }
    public void setType(TypeCommande type) { this.type = type; }

    public StatutCommande getStatut() { return statut; }
    public void setStatut(StatutCommande statut) { this.statut = statut; }

    public LocalDateTime getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDateTime dateCreation) { this.dateCreation = dateCreation; }

    public LocalDateTime getDateLivraison() { return dateLivraison; }
    public void setDateLivraison(LocalDateTime dateLivraison) { this.dateLivraison = dateLivraison; }

    public double getPrixTotal() { return prixTotal; }
    public void setPrixTotal(double prixTotal) { this.prixTotal = prixTotal; }

    public Long getUtilisateurId() { return utilisateurId; }
    public void setUtilisateurId(Long utilisateurId) { this.utilisateurId = utilisateurId; }

    public List<Vetement> getVetements() { return vetements; }
    public void setVetements(List<Vetement> vetements) { this.vetements = vetements; }

    public String getFactureUrl() { return factureUrl; }
    public void setFactureUrl(String factureUrl) { this.factureUrl = factureUrl; }
}
