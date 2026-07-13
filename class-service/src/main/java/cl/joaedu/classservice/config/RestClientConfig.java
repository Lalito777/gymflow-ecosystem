package cl.joaedu.classservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

/**
 * Cliente RestClient hacia membership-service. Se usa como la comunicacion "RestClient"
 * exigida por la pauta (distinta de Feign, que ya se usa en access-service y user-service).
 * Timeouts cortos porque esta llamada bloquea la reserva de una clase: si membership-service
 * no responde rapido, es mejor fallar rapido tambien.
 */
@Configuration
public class RestClientConfig {

    @Bean
    public RestClient membershipRestClient(@Value("${services.membership.url}") String membershipUrl) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(3000);
        factory.setReadTimeout(3000);

        return RestClient.builder()
                .baseUrl(membershipUrl)
                .requestFactory(factory)
                .build();
    }
}
