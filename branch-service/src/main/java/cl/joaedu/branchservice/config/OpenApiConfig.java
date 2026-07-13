package cl.joaedu.branchservice.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI branchServiceOpenApi() {
        return new OpenAPI().info(new Info()
                .title("branch-service - GymFlow")
                .description("Gestion de sucursales de la cadena. Consumido por user-service (Feign) para validar sucursales al registrar socios.")
                .version("1.0"));
    }
}
