package cl.joaedu.membershipservice.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI membershipServiceOpenApi() {
        return new OpenAPI().info(new Info()
                .title("membership-service - GymFlow")
                .description("Planes y membresias de los socios. Expone el estado de la membresia consumido por access-service (Feign) y class-service (RestClient).")
                .version("1.0"));
    }
}
