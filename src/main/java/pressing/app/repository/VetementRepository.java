package pressing.app.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pressing.app.model.CategorieVetement;
import pressing.app.model.Vetement;

import java.util.List;
import java.util.Optional;

/**
 * Repository JPA pour Vetement.
 */
@Repository
public interface VetementRepository extends JpaRepository<Vetement, Long> {

    Optional<Vetement> findBySku(String sku);

    List<Vetement> findByCategorie(CategorieVetement categorie);

    List<Vetement> findByDescriptionContainingIgnoreCase(String terme);

    List<Vetement> findByCouleurContainingIgnoreCase(String couleur);

    /**
     * Recherche globale dans la description, couleur, codeCouleur et sku.
     */
    @Query("SELECT v FROM Vetement v WHERE " +
           "LOWER(v.description) LIKE LOWER(CONCAT('%', :terme, '%')) OR " +
           "LOWER(v.couleur) LIKE LOWER(CONCAT('%', :terme, '%')) OR " +
           "LOWER(v.codeCouleur) LIKE LOWER(CONCAT('%', :terme, '%')) OR " +
           "LOWER(v.sku) LIKE LOWER(CONCAT('%', :terme, '%'))")
    List<Vetement> rechercher(@Param("terme") String terme);

    /**
     * Recuperer tous les vetements d'une commande via la table de jointure.
     */
    @Query("SELECT v FROM Vetement v JOIN v.commandes c WHERE c.id = :commandeId")
    List<Vetement> findByCommandeId(@Param("commandeId") Long commandeId);
}
