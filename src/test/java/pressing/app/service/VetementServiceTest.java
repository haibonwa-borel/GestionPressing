package pressing.app.service;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pressing.app.dto.VetementDTO;
import pressing.app.model.CategorieVetement;
import pressing.app.model.Vetement;
import pressing.app.repository.VetementRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour VetementService.
 * Isole le service avec Mockito.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Tests de VetementService")
class VetementServiceTest {

    @Mock
    private VetementRepository vetementRepository;

    @Mock
    private CommandeService commandeService;

    @Mock
    private GeminiService geminiService;

    @InjectMocks
    private VetementService service;

    private VetementDTO sampleDTO;
    private Vetement sampleEntity;

    @BeforeEach
    void setUp() {
        sampleDTO = new VetementDTO();
        sampleDTO.setDescription("Chemise blanche");
        sampleDTO.setCategorie(CategorieVetement.HAUT);
        sampleDTO.setCouleur("Blanc");
        sampleDTO.setCodeCouleur("#FFFFFF");

        sampleEntity = new Vetement();
        sampleEntity.setId(1L);
        sampleEntity.setSku("VET-ABCD1234");
        sampleEntity.setDescription("Chemise blanche");
        sampleEntity.setCategorie(CategorieVetement.HAUT);
        sampleEntity.setCouleur("Blanc");
        sampleEntity.setCodeCouleur("#FFFFFF");
    }

    @Test
    @DisplayName("Creation d'un vetement avec generation automatique du SKU")
    void creer_sansSkuFourni_genereSkuAutomatiquement() {
        // Arrange
        when(vetementRepository.save(any(Vetement.class))).thenReturn(sampleEntity);

        // Act
        VetementDTO result = service.creer(sampleDTO);

        // Assert
        assertNotNull(result);
        assertEquals("VET-ABCD1234", result.getSku());
        assertEquals("Chemise blanche", result.getDescription());
        assertEquals(CategorieVetement.HAUT, result.getCategorie());
        verify(vetementRepository).save(any(Vetement.class));
    }

    @Test
    @DisplayName("Trouver un vetement par ID existant")
    void trouverParId_existant_retourneDTO() {
        // Arrange
        when(vetementRepository.findById(1L)).thenReturn(Optional.of(sampleEntity));

        // Act
        Optional<VetementDTO> result = service.trouverParId(1L);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getId());
        assertEquals("Chemise blanche", result.get().getDescription());
    }

    @Test
    @DisplayName("Trouver un vetement par ID inexistant retourne vide")
    void trouverParId_inexistant_retourneVide() {
        // Arrange
        when(vetementRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        assertTrue(service.trouverParId(99L).isEmpty());
    }

    @Test
    @DisplayName("Modification d'un vetement existant")
    void modifier_existant_retourneDTO() {
        // Arrange
        VetementDTO updateDTO = new VetementDTO();
        updateDTO.setDescription("Chemise bleue modifiee");
        updateDTO.setCategorie(CategorieVetement.HAUT);
        updateDTO.setCouleur("Bleu");
        updateDTO.setCodeCouleur("#0000FF");

        Vetement updatedEntity = new Vetement();
        updatedEntity.setId(1L);
        updatedEntity.setSku("VET-ABCD1234");
        updatedEntity.setDescription("Chemise bleue modifiee");
        updatedEntity.setCategorie(CategorieVetement.HAUT);
        updatedEntity.setCouleur("Bleu");
        updatedEntity.setCodeCouleur("#0000FF");

        when(vetementRepository.existsById(1L)).thenReturn(true);
        when(vetementRepository.findById(1L)).thenReturn(Optional.of(sampleEntity));
        when(vetementRepository.save(any(Vetement.class))).thenReturn(updatedEntity);

        // Act
        VetementDTO result = service.modifier(1L, updateDTO);

        // Assert
        assertEquals("Chemise bleue modifiee", result.getDescription());
        assertEquals("Bleu", result.getCouleur());
    }

    @Test
    @DisplayName("Modification d'un vetement inexistant leve une exception")
    void modifier_inexistant_leveException() {
        // Arrange
        when(vetementRepository.existsById(99L)).thenReturn(false);

        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> service.modifier(99L, sampleDTO));
    }

    @Test
    @DisplayName("Suppression d'un vetement existant retourne true")
    void supprimer_existant_retourneTrue() {
        // Arrange
        when(vetementRepository.existsById(1L)).thenReturn(true);

        // Act & Assert
        assertTrue(service.supprimer(1L));
        verify(vetementRepository).deleteById(1L);
    }

    @Test
    @DisplayName("Suppression d'un vetement inexistant retourne false")
    void supprimer_inexistant_retourneFalse() {
        // Arrange
        when(vetementRepository.existsById(99L)).thenReturn(false);

        // Act & Assert
        assertFalse(service.supprimer(99L));
        verify(vetementRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("Les assertions groupees verifient tous les champs d'un vetement")
    void creer_verificationGroupee_tousLesChamps() {
        // Arrange
        when(vetementRepository.save(any(Vetement.class))).thenReturn(sampleEntity);

        // Act
        VetementDTO result = service.creer(sampleDTO);

        // Assert - Assertions groupees (JUnit 5 assertAll)
        assertAll("Verification complete du vetement cree",
                () -> assertNotNull(result, "Le resultat ne doit pas etre null"),
                () -> assertEquals("Chemise blanche", result.getDescription(), "Description incorrecte"),
                () -> assertEquals(CategorieVetement.HAUT, result.getCategorie(), "Categorie incorrecte"),
                () -> assertEquals("Blanc", result.getCouleur(), "Couleur incorrecte"),
                () -> assertEquals("#FFFFFF", result.getCodeCouleur(), "Code couleur incorrect"),
                () -> assertNotNull(result.getSku(), "Le SKU ne doit pas etre null")
        );
    }
}
