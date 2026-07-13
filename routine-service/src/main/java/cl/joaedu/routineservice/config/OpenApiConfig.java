package cl.joaedu.routineservice.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI routineServiceOpenApi() {
        return new OpenAPI().info(new Info()
                .title("routine-service - GymFlow")
                .description("Rutinas de entrenamiento asignadas a los socios.")
                .version("1.0"));
    }
}
