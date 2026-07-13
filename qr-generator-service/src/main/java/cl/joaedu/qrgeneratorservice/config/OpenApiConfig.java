package cl.joaedu.qrgeneratorservice.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI qrGeneratorServiceOpenApi() {
        return new OpenAPI().info(new Info()
                .title("qr-generator-service - GymFlow")
                .description("Generacion de imagenes QR (ZXing) para los tokens de acceso de access-service.")
                .version("1.0"));
    }
}
