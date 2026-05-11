package pressing.app.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pressing.app.model.Commande;
import pressing.app.model.StatutCommande;
import pressing.app.model.TypeCommande;

import java.util.List;

/**
 * Repository JPA pour Commande.
 * Inclut la recherche par vetement associe via JPQL.
 */
@Repository
public interface CommandeRepository extends JpaRepository<Commande, Long> {

    List<Commande> findByUtilisateurId(Long utilisateurId);

    List<Commande> findByType(TypeCommande type);

    List<Commande> findByStatut(StatutCommande statut);

    List<Commande> findByTypeAndStatut(TypeCommande type, StatutCommande statut);

    /**
     * Recherche toutes les commandes contenant un vetement donne.
     * Utilise JPQL avec JOIN sur la liste "vetements" de Commande.
     */
    @Query("SELECT c FROM Commande c JOIN c.vetements v WHERE v.id = :vetementId")
    List<Commande> findByVetementId(@Param("vetementId") Long vetementId);
}
