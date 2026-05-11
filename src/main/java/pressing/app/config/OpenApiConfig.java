package pressing.app.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration OpenAPI / Swagger pour la documentation automatique.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI pressingOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("API Pressing - Gestion de Pressing")
                        .description("API REST pour la gestion d'un pressing : utilisateurs, commandes et vetements. TP Spring Boot.")
                        .version("1.0.0"));
    }
}
