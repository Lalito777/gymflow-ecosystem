package cl.joaedu.routineservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class RoutineServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(RoutineServiceApplication.class, args);
    }
}