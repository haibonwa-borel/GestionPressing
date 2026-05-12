package pressing.app.controller;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import pressing.app.dto.PageResponse;
import pressing.app.dto.UtilisateurDTO;
import pressing.app.service.CustomUserDetailsService;
import pressing.app.service.UtilisateurService;

import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests d'integration pour UtilisateurController avec @WebMvcTest.
 * MockMvc simule les requetes HTTP sans demarrer un serveur reel.
 * 
 * @WithMockUser simule un utilisateur authentifie pour les tests.
 * @MockBean injecte des mocks dans le contexte Spring.
 * 
 * Cf. PDF pages 483-491 : Tests d'integration avec MockMvc
 */
@WebMvcTest(UtilisateurController.class)
@DisplayName("Tests d'integration - UtilisateurController")
class UtilisateurControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UtilisateurService utilisateurService;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    private UtilisateurDTO sampleDTO;

    @BeforeEach
    void setUp() {
        sampleDTO = new UtilisateurDTO();
        sampleDTO.setId(1L);
        sampleDTO.setNom("Dupont");
        sampleDTO.setPrenom("Jean");
        sampleDTO.setEmail("jean@email.com");
        sampleDTO.setTelephone("+33612345678");
        sampleDTO.setRole("CLIENT");
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("GET /api/utilisateurs - Liste paginee retourne 200 OK")
    void listerTous_retourne200() throws Exception {
        // Arrange
        PageResponse<UtilisateurDTO> pageResponse = new PageResponse<>(
                List.of(sampleDTO), 0, 10, 1L
        );
        when(utilisateurService.listerAvecPagination(anyInt(), anyInt())).thenReturn(pageResponse);

        // Act & Assert
        mockMvc.perform(get("/api/utilisateurs")
                        .param("page", "0")
                        .param("taille", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contenu", hasSize(1)))
                .andExpect(jsonPath("$.contenu[0].nom").value("Dupont"))
                .andExpect(jsonPath("$.contenu[0].email").value("jean@email.com"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("GET /api/utilisateurs/{id} - Utilisateur existant retourne 200")
    void trouverParId_existant_retourne200() throws Exception {
        // Arrange
        when(utilisateurService.trouverParId(1L)).thenReturn(Optional.of(sampleDTO));

        // Act & Assert
        mockMvc.perform(get("/api/utilisateurs/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nom").value("Dupont"))
                .andExpect(jsonPath("$.prenom").value("Jean"))
                .andExpect(jsonPath("$.email").value("jean@email.com"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("GET /api/utilisateurs/{id} - Utilisateur inexistant retourne 404")
    void trouverParId_inexistant_retourne404() throws Exception {
        // Arrange
        when(utilisateurService.trouverParId(99L)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/api/utilisateurs/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/utilisateurs - Non authentifie retourne 401/302")
    void listerTous_nonAuthentifie_retourneRedirection() throws Exception {
        // Act & Assert - Sans @WithMockUser, l'acces est refuse
        mockMvc.perform(get("/api/utilisateurs")
                        .accept(MediaType.TEXT_HTML))
                .andExpect(status().is3xxRedirection());
    }
}
