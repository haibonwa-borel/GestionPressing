package pressing.app.service;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import pressing.app.dto.UtilisateurDTO;
import pressing.app.model.Utilisateur;
import pressing.app.repository.UtilisateurRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour UtilisateurService.
 * Utilise JUnit 5 et Mockito pour isoler le service de ses dependances.
 * 
 * Structure : Arrange / Act / Assert (AAA)
 * Annotations : @BeforeEach, @Test, @DisplayName
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Tests de UtilisateurService")
class UtilisateurServiceTest {

    @Mock
    private UtilisateurRepository repository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UtilisateurService service;

    private UtilisateurDTO sampleDTO;
    private Utilisateur sampleEntity;

    @BeforeEach
    void setUp() {
        // Arrange - donnees de test communes
        sampleDTO = new UtilisateurDTO();
        sampleDTO.setNom("Dupont");
        sampleDTO.setPrenom("Jean");
        sampleDTO.setEmail("jean@email.com");
        sampleDTO.setMotDePasse("password123");
        sampleDTO.setTelephone("+33612345678");

        sampleEntity = new Utilisateur();
        sampleEntity.setId(1L);
        sampleEntity.setNom("Dupont");
        sampleEntity.setPrenom("Jean");
        sampleEntity.setEmail("jean@email.com");
        sampleEntity.setMotDePasse("$2a$10$encodedPassword");
        sampleEntity.setTelephone("+33612345678");
        sampleEntity.setRole("CLIENT");
        sampleEntity.setActif(true);
    }

    // ======== TESTS DE CREATION ========

    @Test
    @DisplayName("Creation d'un utilisateur avec succes")
    void creer_avecDonneesValides_retourneDTO() {
        // Arrange
        when(repository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(repository.findByTelephone(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("$2a$10$encodedPassword");
        when(repository.save(any(Utilisateur.class))).thenReturn(sampleEntity);

        // Act
        UtilisateurDTO result = service.creer(sampleDTO);

        // Assert
        assertNotNull(result);
        assertEquals("Dupont", result.getNom());
        assertEquals("Jean", result.getPrenom());
        assertEquals("jean@email.com", result.getEmail());
        assertEquals("CLIENT", result.getRole());
        assertNull(result.getMotDePasse(), "Le mot de passe ne doit jamais etre retourne");

        // Verification : le passwordEncoder a ete appele
        verify(passwordEncoder).encode("password123");
        verify(repository).save(any(Utilisateur.class));
    }

    @Test
    @DisplayName("Creation echoue si email deja existant")
    void creer_avecEmailExistant_leveException() {
        // Arrange
        when(repository.findByEmail("jean@email.com")).thenReturn(Optional.of(sampleEntity));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.creer(sampleDTO),
                "Devrait lever une exception pour email duplique"
        );
        assertEquals("Un utilisateur avec cet email existe deja", exception.getMessage());
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Creation echoue si telephone deja existant")
    void creer_avecTelephoneExistant_leveException() {
        // Arrange
        when(repository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(repository.findByTelephone("+33612345678")).thenReturn(Optional.of(sampleEntity));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> service.creer(sampleDTO));
        verify(repository, never()).save(any());
    }

    // ======== TESTS DE RECHERCHE ========

    @Test
    @DisplayName("Trouver un utilisateur par ID existant")
    void trouverParId_idExistant_retourneDTO() {
        // Arrange
        when(repository.findById(1L)).thenReturn(Optional.of(sampleEntity));

        // Act
        Optional<UtilisateurDTO> result = service.trouverParId(1L);

        // Assert
        assertTrue(result.isPresent());
        assertEquals("Dupont", result.get().getNom());
        assertEquals(1L, result.get().getId());
    }

    @Test
    @DisplayName("Trouver un utilisateur par ID inexistant retourne vide")
    void trouverParId_idInexistant_retourneVide() {
        // Arrange
        when(repository.findById(99L)).thenReturn(Optional.empty());

        // Act
        Optional<UtilisateurDTO> result = service.trouverParId(99L);

        // Assert
        assertTrue(result.isEmpty());
    }

    // ======== TESTS DE MODIFICATION ========

    @Test
    @DisplayName("Modification d'un utilisateur avec succes")
    void modifier_avecDonneesValides_retourneDTO() {
        // Arrange
        UtilisateurDTO updateDTO = new UtilisateurDTO();
        updateDTO.setNom("Dupont-Modifie");
        updateDTO.setPrenom("Jean");
        updateDTO.setEmail("jean@email.com");
        updateDTO.setTelephone("+33612345678");
        // motDePasse null -> conserver l'ancien

        Utilisateur updatedEntity = new Utilisateur();
        updatedEntity.setId(1L);
        updatedEntity.setNom("Dupont-Modifie");
        updatedEntity.setPrenom("Jean");
        updatedEntity.setEmail("jean@email.com");
        updatedEntity.setMotDePasse("$2a$10$encodedPassword");
        updatedEntity.setTelephone("+33612345678");
        updatedEntity.setRole("CLIENT");

        when(repository.findById(1L)).thenReturn(Optional.of(sampleEntity));
        when(repository.save(any(Utilisateur.class))).thenReturn(updatedEntity);

        // Act
        UtilisateurDTO result = service.modifier(1L, updateDTO);

        // Assert
        assertEquals("Dupont-Modifie", result.getNom());
        // Le password encoder ne doit PAS etre appele car motDePasse est null
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    @DisplayName("Modification avec nouveau mot de passe encode en BCrypt")
    void modifier_avecNouveauMotDePasse_encodeAvecBCrypt() {
        // Arrange
        UtilisateurDTO updateDTO = new UtilisateurDTO();
        updateDTO.setNom("Dupont");
        updateDTO.setPrenom("Jean");
        updateDTO.setEmail("jean@email.com");
        updateDTO.setMotDePasse("newPassword456");
        updateDTO.setTelephone("+33612345678");

        when(repository.findById(1L)).thenReturn(Optional.of(sampleEntity));
        when(passwordEncoder.encode("newPassword456")).thenReturn("$2a$10$newEncodedPassword");
        when(repository.save(any(Utilisateur.class))).thenReturn(sampleEntity);

        // Act
        service.modifier(1L, updateDTO);

        // Assert - le password encoder DOIT etre appele avec le nouveau mot de passe
        verify(passwordEncoder).encode("newPassword456");
    }

    @Test
    @DisplayName("Modification echoue si utilisateur non trouve")
    void modifier_utilisateurInexistant_leveException() {
        // Arrange
        when(repository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> service.modifier(99L, sampleDTO));
    }

    // ======== TESTS DE SUPPRESSION ========

    @Test
    @DisplayName("Suppression d'un utilisateur existant retourne true")
    void supprimer_idExistant_retourneTrue() {
        // Arrange
        when(repository.existsById(1L)).thenReturn(true);

        // Act
        boolean result = service.supprimer(1L);

        // Assert
        assertTrue(result);
        verify(repository).deleteById(1L);
    }

    @Test
    @DisplayName("Suppression d'un utilisateur inexistant retourne false")
    void supprimer_idInexistant_retourneFalse() {
        // Arrange
        when(repository.existsById(99L)).thenReturn(false);

        // Act
        boolean result = service.supprimer(99L);

        // Assert
        assertFalse(result);
        verify(repository, never()).deleteById(any());
    }
}
