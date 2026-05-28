# GymFlow Ecosystem

Sistema distribuido de gestión integral de gimnasios basado en arquitectura de microservicios.

## Integrantes

- Joaquín Sandoval
- eduardo sepulveda

## Tecnologías

- Java 17
- Spring Boot 3.2.5
- Spring Cloud 2023.0.1 (Eureka, Feign Client)
- Spring Security + JWT
- JPA / Hibernate
- Flyway
- H2 Database (en memoria)
- ZXing (generación de códigos QR)
- Maven

## Microservicios

| Servicio | Puerto | Descripción |
|----------|--------|-------------|
| eureka-server | 8761 | Registro y descubrimiento de servicios |
| user-service | 8080 | Autenticación y gestión de usuarios |
| branch-service | 8081 | Sucursales físicas del gimnasio |
| membership-service | 8083 | Planes y membresías |
| access-service | 8084 | Control de acceso con QR |
| qr-generator-service | 8085 | Generación de códigos QR |
| capacity-service | 8086 | Control de aforo |
| class-service | 8087 | Clases grupales |
| routine-service | 8088 | Rutinas de entrenamiento |
| equipment-service | 8089 | Inventario de equipamiento |
| notification-service | 8090 | Notificaciones a usuarios |

## Funcionalidades implementadas

- Registro y autenticación de usuarios con JWT
- Gestión de sucursales y membresías
- Generación y validación de códigos QR de acceso
- Control de aforo en tiempo real
- Gestión de clases, rutinas y equipamiento
- Sistema de notificaciones

