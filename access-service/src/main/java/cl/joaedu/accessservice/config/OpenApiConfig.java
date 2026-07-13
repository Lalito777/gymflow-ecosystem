package cl.joaedu.accessservice.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI accessServiceOpenApi() {
        return new OpenAPI().info(new Info()
                .title("access-service - GymFlow")
                .description("Emision y validacion de tokens de acceso por QR. Valida membresia activa (Feign) e incrementa aforo (Feign) al validar una entrada.")
                .version("1.0"));
    }
}
