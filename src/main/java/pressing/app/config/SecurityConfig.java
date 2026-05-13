package pressing.app.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import pressing.app.security.JwtAuthenticationFilter;

/**
 * Configuration Spring Security avec deux chaînes de filtres :
 *
 *  1. Chaîne API  (@Order(1)) : /api/**  — Stateless, JWT Bearer token
 *  2. Chaîne Web  (@Order(2)) : tout le reste — Session + formulaire HTML
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * BCrypt pour l'encodage des mots de passe.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * AuthenticationManager partagé entre les deux chaînes.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    // =========================================================================
    // CHAÎNE 1 — API REST (/api/**) — JWT, Stateless
    // =========================================================================
    @Bean
    @Order(1)
    public SecurityFilterChain apiFilterChain(HttpSecurity http,
                                               JwtAuthenticationFilter jwtFilter) throws Exception {
        http
            .securityMatcher("/api/**")

            // Pas de session côté serveur pour l'API
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            // CSRF inutile en mode stateless
            .csrf(csrf -> csrf.disable())

            // Autorisations
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/login", "/api/logout").permitAll()
                // Le chatbot est appelé depuis l'UI web (auth par session, pas JWT)
                .requestMatchers("/api/chat").permitAll()
                .anyRequest().authenticated()
            )

            // Retourner 401 JSON (pas une redirection vers /login)
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
            )

            // Injecter le filtre JWT avant le filtre d'authentification par formulaire
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // =========================================================================
    // CHAÎNE 2 — Interface Web (Thymeleaf) — Session + formulaire
    // =========================================================================
    @Bean
    @Order(2)
    public SecurityFilterChain webFilterChain(HttpSecurity http) throws Exception {
        http
            // === AUTORISATION ===
            .authorizeHttpRequests(auth -> auth
                // Pages et ressources publiques
                .requestMatchers("/login", "/register", "/error",
                                 "/css/**", "/js/**", "/images/**", "/uploads/**").permitAll()
                // Swagger UI
                .requestMatchers("/swagger-ui/**", "/swagger-ui.html",
                                 "/api-docs/**", "/v3/api-docs/**").permitAll()
                // Administration
                .requestMatchers("/admin/**").hasRole("ADMIN")
                // Tout le reste nécessite une authentification
                .anyRequest().authenticated()
            )

            // === FORMULAIRE DE CONNEXION (Web) ===
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .usernameParameter("email")
                .passwordParameter("motDePasse")
                .defaultSuccessUrl("/", true)
                .failureUrl("/login?error=true")
                .permitAll()
            )

            // === DÉCONNEXION (Web) ===
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout=true")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            )

            // === CSRF (activé pour le Web, désactivé pour /api via l'autre chaîne) ===
            .csrf(csrf -> csrf
                .ignoringRequestMatchers("/login", "/logout")
            )

            // === REMEMBER ME ===
            .rememberMe(remember -> remember
                .key("pressing-app-remember-me-key")
                .tokenValiditySeconds(86400)
            );

        return http.build();
    }
}
