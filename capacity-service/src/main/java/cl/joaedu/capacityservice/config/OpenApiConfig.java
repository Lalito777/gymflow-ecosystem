package cl.joaedu.capacityservice.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI capacityServiceOpenApi() {
        return new OpenAPI().info(new Info()
                .title("capacity-service - GymFlow")
                .description("Aforo en tiempo real por sucursal. Incrementado/decrementado en vivo por access-service.")
                .version("1.0"));
    }
}
