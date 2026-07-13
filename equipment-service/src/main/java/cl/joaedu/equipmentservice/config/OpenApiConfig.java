package cl.joaedu.equipmentservice.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI equipmentServiceOpenApi() {
        return new OpenAPI().info(new Info()
                .title("equipment-service - GymFlow")
                .description("Inventario de equipos de gimnasio por sucursal.")
                .version("1.0"));
    }
}
