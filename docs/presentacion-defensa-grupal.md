# Presentación de defensa grupal — GymFlow (guion / outline de slides)

> Esto es un esqueleto listo para convertir en slides (pptx/Google Slides). Cada `##` es una
> diapositiva. Completar con capturas reales del sistema corriendo antes de presentar.

## 1. Portada
GymFlow — Sistema de gestión de gimnasio multisucursal. DSY1103 Full Stack 1, Duoc UC.
Nombres del equipo.

## 2. Problema y alcance
Una cadena de gimnasios necesita: gestionar socios y sucursales, controlar acceso físico con
QR, aforo en tiempo real, reservas de clases, rutinas, inventario y notificaciones — todo
integrado, no sistemas sueltos.

## 3. Arquitectura general
Diagrama: Gateway → Eureka → 10 servicios de dominio. (Usar el diagrama de `README.md`.)

## 4. Stack tecnológico
Java 17, Spring Boot 3.2.5, Spring Cloud 2023.0.1, Eureka, Gateway, Postgres/H2, Docker, Render.

## 5. Los 12 microservicios (una tabla, ver README.md)

## 6. Comunicación entre servicios
Feign (`user→branch`, `access→capacity/membership`) vs RestClient (`class→membership`) —
cuándo se usa cada uno y por qué.

## 7. Seguridad
Roles SOCIO/ADMIN, BCrypt, 401 vs 403, endpoints protegidos.

## 8. Calidad: pruebas y documentación
Cobertura de tests en capa `service`, Swagger en los 12 servicios, manejo uniforme de errores.

## 9. Despliegue
Docker Compose local + Render remoto (Blueprint automático con `render.yaml`).

## 10. Demo en vivo
Sugerencia de flujo para la demo: crear usuario → crear membresía → generar QR → validar
acceso → reservar clase. (Usar `docs/gymflow.http`.)

## 11. Cierre
Aprendizajes del equipo, dificultades técnicas resueltas (mencionar el caso Feign muerto /
seguridad decorativa corregidos), próximos pasos si el proyecto continuara.
