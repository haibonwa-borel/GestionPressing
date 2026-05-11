package pressing.app.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pressing.app.model.Utilisateur;

import java.util.Optional;

/**
 * Repository JPA pour Utilisateur.
 * Spring Data genere automatiquement les requetes SQL.
 */
@Repository
public interface UtilisateurRepository extends JpaRepository<Utilisateur, Long> {

    Optional<Utilisateur> findByEmail(String email);

    Optional<Utilisateur> findByTelephone(String telephone);
}
