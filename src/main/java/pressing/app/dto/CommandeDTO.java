package pressing.app.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import pressing.app.model.StatutCommande;
import pressing.app.model.TypeCommande;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO pour la Commande.
 * La reponse inclut les vetements complets (pas seulement les IDs).
 */
public class CommandeDTO {

    private Long id;

    @NotNull(message = "Le type de commande est obligatoire")
    private TypeCommande type;

    private StatutCommande statut;
    private LocalDateTime dateCreation;
    private LocalDateTime dateLivraison;

    @Min(value = 0, message = "Le prix total doit etre positif ou nul")
    private double prixTotal;

    @NotNull(message = "L'identifiant utilisateur est obligatoire")
    private Long utilisateurId;

    // IDs en ecriture seule (pour les formulaires de creation/modification)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private List<Long> vetementIds = new ArrayList<>();

    // Objets complets en lecture seule (dans la reponse JSON)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private List<VetementDTO> vetements = new ArrayList<>();

    public CommandeDTO() {}

    // --- Getters et Setters ---
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

    public List<Long> getVetementIds() { return vetementIds; }
    public void setVetementIds(List<Long> vetementIds) { this.vetementIds = vetementIds; }

    public List<VetementDTO> getVetements() { return vetements; }
    public void setVetements(List<VetementDTO> vetements) { this.vetements = vetements; }
}
