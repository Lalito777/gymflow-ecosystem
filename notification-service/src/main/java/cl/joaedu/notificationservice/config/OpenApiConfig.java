package cl.joaedu.notificationservice.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI notificationServiceOpenApi() {
        return new OpenAPI().info(new Info()
                .title("notification-service - GymFlow")
                .description("Envio de notificaciones (EMAIL, SMS, PUSH) a los socios.")
                .version("1.0"));
    }
}
