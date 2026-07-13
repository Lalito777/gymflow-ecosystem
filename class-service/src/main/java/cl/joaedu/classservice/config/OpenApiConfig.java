package cl.joaedu.classservice.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI classServiceOpenApi() {
        return new OpenAPI().info(new Info()
                .title("class-service - GymFlow")
                .description("Reserva de clases grupales. Valida membresia activa contra membership-service via RestClient antes de confirmar la reserva.")
                .version("1.0"));
    }
}
