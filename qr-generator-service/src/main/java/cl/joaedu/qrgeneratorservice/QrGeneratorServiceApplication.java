package cl.joaedu.qrgeneratorservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class QrGeneratorServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(QrGeneratorServiceApplication.class, args);
    }
}