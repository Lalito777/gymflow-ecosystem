# Matriz de cumplimiento — requisitos obligatorios de la pauta EFT

Estado al cierre del trabajo (13 jul 2026). Compara con el diagnóstico inicial en
`checklist-diagnostico.md`, que registra el estado ANTES de esta sesión de trabajo.

| Requisito obligatorio | Estado inicial | Estado final | Evidencia |
|---|---|---|---|
| Patrón CSR completo (controller→service→repository) | Parcial (8/10) | ✅ 10/10 | `equipment-service` y `notification-service` ahora tienen `service/EquipmentService.java` y `service/NotificationService.java` |
| DTOs separados de entidad (creación/respuesta) | ❌ 2/10 | ✅ 10/10 | Cada servicio tiene paquete `dto/` con `*Request`/`*Response`; ningún controller expone la entidad JPA ni recibe `Map` como body |
| Bean Validation (`@NotNull`, etc.) + `@Valid` en controller | ❌ 0/10 | ✅ 10/10 | Anotaciones `jakarta.validation.constraints` en los DTOs de request + `@Valid` en los métodos del controller |
| `@ControllerAdvice` con JSON de error uniforme | Parcial (2/10) | ✅ 10/10 | `config/GlobalExceptionHandler.java` en cada servicio, mismo shape `ErrorResponse(timestamp, status, error, message, path, fields)` |
| Comunicación Feign real (invocada, no solo declarada) | Parcial | ✅ | `user-service → branch-service` (antes código muerto, ahora `UserService.verifyBranchExists()` lo invoca en cada alta de usuario); `access-service → capacity-service` / `→ membership-service` ya funcionaban |
| Comunicación RestClient real | ❌ 0/12 | ✅ | `class-service → membership-service` con `RestClient` (Spring 3.2+), timeout 3s, manejo de error si no responde |
| Seguridad real (roles, reglas por endpoint, 401/403) | ❌ Superficial (`permitAll`) | ✅ | `user-service`: BCrypt, roles SOCIO/ADMIN, `GET /api/users` solo ADMIN, 401 (no autenticado) y 403 (sin permiso) devuelven JSON uniforme vía `AuthenticationEntryPoint`/`AccessDeniedHandler` |
| Gateway: rutas `lb://` | ✅ | ✅ | Sin cambios, ya estaba correcto |
| Gateway: filtro `X-Request-Id` / trazabilidad | ❌ | ✅ | `filter/RequestTraceFilter.java` (GlobalFilter + Ordered), agrega o respeta el header en cada request |
| Eureka + registro dinámico | ✅ | ✅ | Sin cambios |
| Flyway versionado | ✅ | ✅ | `user-service` suma `V3__add_branch_to_users.sql` y `V4__seed_users.sql` (seed real con BCrypt, antes vivía en `import.sql` que nunca se ejecutaba con `ddl-auto: validate`) |
| Swagger con `@Operation`/`@ApiResponse` | ❌ 0/12 | ✅ 12/12 | `@Tag`, `@Operation`, `@ApiResponses` en todos los controllers + `config/OpenApiConfig.java` por servicio |
| `System.out.println` como logging | ✅ (no había) | ✅ | Se mantiene SLF4J en todos los servicios |
| Pruebas unitarias | Parcial (5/10) | ✅ 10/10 | Tests nuevos en `capacity`, `class`, `routine`, `equipment`, `notification`; todos verificados con `mvnw test` → BUILD SUCCESS |
| Driver Postgres en `pom.xml` | ❌ 0/12 | ✅ 4/4 (servicios con datos críticos) | `user`, `branch`, `membership`, `access` suman `org.postgresql:postgresql` |
| Despliegue en entorno remoto (Render) | ❌ | ✅ preparado, pendiente de deploy real | `render.yaml` con los 12 servicios + Postgres, `Dockerfile` de cada uno lee `PORT` dinámico |
| `.gitignore` protege secretos | Por confirmar | ✅ | Se agregó `.env`, `.env.*` (excepto `.env.example`) |

## Ítems que quedaron pendientes de verificación final

- **Despliegue real en Render**: el código y el Blueprint están listos; falta que Eduardo cree
  la cuenta y ejecute el Blueprint desde el dashboard (paso manual, ver
  `documentacion-tecnica.md`).
- **Docker Compose end-to-end**: se corrigieron dos variables de entorno que faltaban
  (`BRANCH_SERVICE_URL` en `user-service`, `MEMBERSHIP_SERVICE_URL` en `class-service`) pero no
  se volvió a correr el stack completo tras el cambio — recomendado antes de la defensa.
