package pressing.app.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

/**
 * Configuration de Spring Security.
 * - Formulaire de connexion personnalise (/login)
 * - Protection CSRF activee
 * - Roles ADMIN / CLIENT pour l'autorisation
 * - BCrypt pour l'encodage des mots de passe
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * Bean PasswordEncoder utilisant BCrypt.
     * BCrypt est l'algorithme recommande pour sa resistance
     * aux attaques par force brute et rainbow tables.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Bean AuthenticationManager.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * Configuration de la chaine de filtres de securite.
     * Definit les regles d'autorisation, le formulaire de login,
     * la protection CSRF et la gestion du logout.
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // === AUTORISATION ===
            .authorizeHttpRequests(auth -> auth
                // Pages et ressources publiques
                .requestMatchers("/login", "/register", "/error", "/css/**", "/js/**", "/images/**", "/uploads/**").permitAll()
                // API Swagger publique
                .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/api-docs/**", "/v3/api-docs/**").permitAll()
                // Routes administration reservees aux ADMIN
                .requestMatchers("/admin/**").hasRole("ADMIN")
                // API REST : accessibles aux utilisateurs authentifies
                .requestMatchers("/api/**").authenticated()
                // Toutes les autres requetes necessitent une authentification
                .anyRequest().authenticated()
            )

            // === FORMULAIRE DE CONNEXION ===
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .usernameParameter("email")
                .passwordParameter("motDePasse")
                .defaultSuccessUrl("/", true)
                .failureUrl("/login?error=true")
                .permitAll()
            )

            // === DECONNEXION ===
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout=true")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            )

            // === PROTECTION CSRF ===
            // CSRF active par defaut. Configuration pour les meta tags AJAX.
            .csrf(csrf -> csrf
                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                // Desactiver CSRF uniquement pour les routes API REST
                .ignoringRequestMatchers("/api/**")
            )

            // === REMEMBER ME ===
            .rememberMe(remember -> remember
                .key("pressing-app-remember-me-key")
                .tokenValiditySeconds(86400) // 24 heures
            );

        return http.build();
    }
}
