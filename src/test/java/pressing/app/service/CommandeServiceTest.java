package pressing.app.service;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pressing.app.dto.CommandeDTO;
import pressing.app.model.*;
import pressing.app.repository.CommandeRepository;
import pressing.app.repository.VetementRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour CommandeService.
 * Isole le service avec Mockito pour tester la logique metier.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Tests de CommandeService")
class CommandeServiceTest {

    @Mock
    private CommandeRepository commandeRepository;

    @Mock
    private VetementRepository vetementRepository;

    @InjectMocks
    private CommandeService service;

    private Commande sampleCommande;

    @BeforeEach
    void setUp() {
        sampleCommande = new Commande();
        sampleCommande.setId(1L);
        sampleCommande.setType(TypeCommande.NORMAL);
        sampleCommande.setStatut(StatutCommande.EN_ATTENTE);
        sampleCommande.setUtilisateurId(1L);
        sampleCommande.setDateCreation(LocalDateTime.now());
        sampleCommande.setDateLivraison(LocalDateTime.now().plusDays(7));
        sampleCommande.setPrixTotal(2000.0);
        sampleCommande.setVetements(new ArrayList<>());
    }

    @Test
    @DisplayName("Creation d'une commande NORMAL avec 2 vetements")
    void creer_commandeNormale_calculeCorrectement() {
        // Arrange
        CommandeDTO dto = new CommandeDTO();
        dto.setType(TypeCommande.NORMAL);
        dto.setUtilisateurId(1L);
        dto.setVetementIds(List.of(1L, 2L));

        Vetement v1 = new Vetement();
        v1.setId(1L);
        Vetement v2 = new Vetement();
        v2.setId(2L);

        when(vetementRepository.findAllById(List.of(1L, 2L))).thenReturn(List.of(v1, v2));
        
        Commande savedCommande = new Commande();
        savedCommande.setId(1L);
        savedCommande.setType(TypeCommande.NORMAL);
        savedCommande.setStatut(StatutCommande.EN_ATTENTE);
        savedCommande.setUtilisateurId(1L);
        savedCommande.setDateCreation(LocalDateTime.now());
        savedCommande.setDateLivraison(LocalDateTime.now().plusDays(7));
        savedCommande.setPrixTotal(2000.0); // 2 * 1000 * 1.0
        savedCommande.setVetements(List.of(v1, v2));
        
        when(commandeRepository.save(any(Commande.class))).thenReturn(savedCommande);

        // Act
        CommandeDTO result = service.creer(dto);

        // Assert
        assertNotNull(result);
        assertEquals(TypeCommande.NORMAL, result.getType());
        assertEquals(StatutCommande.EN_ATTENTE, result.getStatut());
        assertEquals(2000.0, result.getPrixTotal());
        assertEquals(2, result.getVetementIds().size());
    }

    @Test
    @DisplayName("Creation d'une commande RAPIDE - prix multiplie par 1.5")
    void creer_commandeRapide_prixMultiplie() {
        // Arrange
        CommandeDTO dto = new CommandeDTO();
        dto.setType(TypeCommande.RAPIDE);
        dto.setUtilisateurId(1L);
        dto.setVetementIds(List.of(1L));

        Vetement v1 = new Vetement();
        v1.setId(1L);
        when(vetementRepository.findAllById(List.of(1L))).thenReturn(List.of(v1));

        Commande savedCommande = new Commande();
        savedCommande.setId(2L);
        savedCommande.setType(TypeCommande.RAPIDE);
        savedCommande.setStatut(StatutCommande.EN_ATTENTE);
        savedCommande.setUtilisateurId(1L);
        savedCommande.setDateCreation(LocalDateTime.now());
        savedCommande.setDateLivraison(LocalDateTime.now().plusDays(4));
        savedCommande.setPrixTotal(1500.0); // 1 * 1000 * 1.5
        savedCommande.setVetements(List.of(v1));

        when(commandeRepository.save(any(Commande.class))).thenReturn(savedCommande);

        // Act
        CommandeDTO result = service.creer(dto);

        // Assert
        assertEquals(1500.0, result.getPrixTotal());
    }

    @Test
    @DisplayName("Trouver une commande par ID existant")
    void trouverParId_existant_retourneDTO() {
        // Arrange
        when(commandeRepository.findById(1L)).thenReturn(Optional.of(sampleCommande));

        // Act
        Optional<CommandeDTO> result = service.trouverParId(1L);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getId());
        assertEquals(TypeCommande.NORMAL, result.get().getType());
    }

    @Test
    @DisplayName("Trouver une commande par ID inexistant retourne vide")
    void trouverParId_inexistant_retourneVide() {
        // Arrange
        when(commandeRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        assertTrue(service.trouverParId(99L).isEmpty());
    }

    @Test
    @DisplayName("Modification d'une commande existante")
    void modifier_commandeExistante_retourneDTO() {
        // Arrange
        CommandeDTO dto = new CommandeDTO();
        dto.setType(TypeCommande.EXPRESS);
        dto.setStatut(StatutCommande.EN_COURS);
        dto.setUtilisateurId(1L);

        Commande updatedCommande = new Commande();
        updatedCommande.setId(1L);
        updatedCommande.setType(TypeCommande.EXPRESS);
        updatedCommande.setStatut(StatutCommande.EN_COURS);
        updatedCommande.setUtilisateurId(1L);
        updatedCommande.setDateCreation(sampleCommande.getDateCreation());
        updatedCommande.setDateLivraison(sampleCommande.getDateCreation().plusDays(1));
        updatedCommande.setPrixTotal(0.0);
        updatedCommande.setVetements(new ArrayList<>());

        when(commandeRepository.findById(1L)).thenReturn(Optional.of(sampleCommande));
        when(commandeRepository.save(any(Commande.class))).thenReturn(updatedCommande);

        // Act
        CommandeDTO result = service.modifier(1L, dto);

        // Assert
        assertEquals(TypeCommande.EXPRESS, result.getType());
        assertEquals(StatutCommande.EN_COURS, result.getStatut());
    }

    @Test
    @DisplayName("Modification d'une commande inexistante leve une exception")
    void modifier_commandeInexistante_leveException() {
        // Arrange
        when(commandeRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> service.modifier(99L, new CommandeDTO()));
    }

    @Test
    @DisplayName("Suppression d'une commande existante retourne true")
    void supprimer_existante_retourneTrue() {
        // Arrange
        when(commandeRepository.existsById(1L)).thenReturn(true);

        // Act & Assert
        assertTrue(service.supprimer(1L));
        verify(commandeRepository).deleteById(1L);
    }

    @Test
    @DisplayName("Suppression d'une commande inexistante retourne false")
    void supprimer_inexistante_retourneFalse() {
        // Arrange
        when(commandeRepository.existsById(99L)).thenReturn(false);

        // Act & Assert
        assertFalse(service.supprimer(99L));
        verify(commandeRepository, never()).deleteById(any());
    }
}
