# GymFlow Ecosystem

Sistema de gestión de gimnasios construido con arquitectura de microservicios independientes (Spring Boot 3.2.5 / Spring Cloud 2023.0.1, Java 17).

## Arquitectura

Cada microservicio tiene su propia base de datos (H2 en memoria) y se comunica vía REST, ya sea directamente (Feign Client) o a través del API Gateway. El descubrimiento de servicios se realiza con Eureka.

| Servicio | Puerto | Responsabilidad |
|---|---|---|
| eureka-server | 8761 | Service Discovery |
| gateway-service | 8082 | API Gateway (Spring Cloud Gateway) |
| user-service | 8080 | Gestión de usuarios/socios y autenticación |
| branch-service | 8081 | Gestión de sucursales |
| membership-service | 8083 | Planes y membresías |
| access-service | 8084 | Tokens de acceso y validación de entrada |
| qr-generator-service | 8085 | Generación de códigos QR |
| capacity-service | 8086 | Aforo en tiempo real por sucursal |
| class-service | 8087 | Reserva de clases |
| routine-service | 8088 | Rutinas de entrenamiento |
| equipment-service | 8089 | Inventario de equipos |
| notification-service | 8090 | Notificaciones |

## Patrón

Cada servicio sigue el patrón **Controller – Service – Repository (CSR)** con entidades JPA, migraciones Flyway y base de datos H2 propia (Database per Service).

## Cómo levantar el proyecto

1. Requisitos: Java 17, Maven (incluido vía wrapper `mvnw`).
2. Ejecutar `start-all.cmd` desde la raíz del proyecto: levanta Eureka, el Gateway y los 10 microservicios en ventanas independientes.
3. Verificar registro de servicios en Eureka: `http://localhost:8761`
4. Todo el tráfico externo puede entrar por el Gateway: `http://localhost:8082`

## Documentación de API (Swagger/OpenAPI)

Cada microservicio expone su documentación interactiva en:
```
http://localhost:<puerto>/swagger-ui.html
```

## Tests

Tests unitarios (JUnit 5 + Mockito, esquema Given-When-Then) en los servicios: `user-service`, `branch-service`, `membership-service`, `access-service`, `qr-generator-service`.

Ejecutar en cada servicio:
```
./mvnw test
```

## Stack técnico

Spring Boot, Spring Cloud Gateway, Spring Cloud Netflix Eureka, Spring Data JPA, Spring Security, OpenFeign, Flyway, H2 Database, springdoc-openapi, JUnit 5, Mockito, ZXing (generación de QR).
