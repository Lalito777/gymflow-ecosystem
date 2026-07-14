# Levantamiento de requerimientos — actualizado

**Nota sobre este documento:** el levantamiento original de inicio de semestre no está disponible
al momento de esta entrega. En vez de presentar como "original" algo que no lo es, este documento
se **reconstruye desde el estado real del código** en su punto más temprano (commit `1e55687`,
27-05-2026: "estructura inicial del ecosistema GymFlow con 10 microservicios") y traza cómo
evolucionó cada requerimiento hasta la entrega final. Es honesto y defendible: cada fila se puede
verificar contra el historial de git.

| ID | Requerimiento base (estado inicial, commit `1e55687`) | Cambio realizado | Justificación | Estado final | Evidencia |
|---|---|---|---|---|---|
| LR-01 | 10 microservicios de dominio con CRUD básico, entidades expuestas directo en los controllers | Se separaron DTOs de entidad en los 8 servicios que no los tenían, con Bean Validation | Exponer la entidad JPA acopla la API al modelo de datos interno; es una mala práctica que la pauta exige corregir | Completado | `dto/` en los 12 servicios, ver `matriz-requerimientos.md` RF-01 a RF-19 |
| LR-02 | Sin API Gateway; cada servicio se llamaba por su puerto directo | Se agregó `gateway-service` con rutas `lb://` vía Eureka | Punto de entrada único, balanceo de carga, y requisito explícito de la pauta | Completado | commit `a43d7b1`, `render.yaml` |
| LR-03 | Sin autenticación; cualquiera podía llamar cualquier endpoint | Se implementó Basic Auth + BCrypt + roles SOCIO/ADMIN en `user-service` | Requisito de seguridad de la pauta; sin esto RNF-07 queda en 0 | Completado | `SecurityConfig.java`, ver RNF-07 en `matriz-requerimientos.md` |
| LR-04 | Sin pruebas unitarias en ningún servicio | Se agregaron 42 tests JUnit 5 + Mockito en los 10 servicios de dominio | Feedback explícito de la 3ª evaluación (solo 5/12 tenían pruebas) | Completado | commits `87df134` y los agregados en el cierre; ver FB-03 en `plan-cierre-feedback.md` |
| LR-05 | Sin documentación de API (Swagger sin anotar) | Se anotaron los 12 servicios con `@Operation`/`@ApiResponses` | Feedback explícito de la 3ª evaluación (FB-04) | Completado | commit `dd93a14` + `OpenApiConfig.java` por servicio |
| LR-06 | `equipment-service` y `notification-service` sin capa `service` (controller hablaba directo con el repository) | Se creó la capa `service` en ambos | Feedback explícito de la 3ª evaluación (FB-01, FB-02) | Completado | `EquipmentService.java`, `NotificationService.java` |
| LR-07 | `BranchClient` (Feign) declarado pero nunca invocado desde `user-service` | Se conectó realmente: `UserService` valida que la sucursal exista antes de crear un usuario | Feedback explícito de la 3ª evaluación (FB-05): código muerto que aparentaba cumplir un requisito | Completado | `UserService.verifyBranchExists()`, ver FB-05 |
| LR-08 | Sin comunicación con Spring `RestClient` (solo Feign) | Se agregó `class-service → membership-service` vía `RestClient` | La pauta pide demostrar ambas formas de comunicación HTTP saliente que ofrece Spring | Completado | `RestClientConfig.java` en `class-service` |
| LR-09 | Todos los servicios en H2 en memoria (datos se pierden al reiniciar) | Los 4 servicios con datos críticos (`user`, `branch`, `membership`, `access`) migraron a Postgres real, con schema propio por servicio | Persistencia real para datos de negocio importantes; los otros 8 manejan datos operativos/efímeros y se dejaron en H2 por alcance/tiempo | Completado (parcial por decisión, ver justificación en `plan-cierre-feedback.md`) | `application-render.yml`, Flyway `V1...V4` |
| LR-10 | Sin despliegue remoto; el proyecto solo corría local con Docker Compose | Despliegue de los 12 servicios + Gateway + Eureka + Postgres en Render | Requisito de entrega de la EFT | Completado | `render.yaml`, URLs públicas `*.onrender.com` |
| LR-11 | Manejo de errores inconsistente entre servicios (algunos devolvían el stacktrace crudo) | `GlobalExceptionHandler` uniforme (`ErrorResponse` estándar) en los 12 servicios | Requisito no funcional de manejo uniforme de errores | Completado | Ver RNF-05 en `matriz-requerimientos.md` |
| LR-12 | Sin trazabilidad de una request entre servicios | Filtro `X-Request-Id` en el Gateway | Permite correlacionar logs cuando falla una cadena de llamadas entre servicios | Completado | `RequestTraceFilter.java` en `gateway-service` |

## Qué no cambió

El alcance funcional base (gestión de usuarios, sucursales, membresías, control de acceso por
QR, clases, rutinas, equipos y notificaciones) se mantuvo igual desde el commit inicial — lo que
cambió fue la calidad de la implementación (seguridad, validación, pruebas, comunicación real
entre servicios, persistencia y despliegue), no el conjunto de funcionalidades del sistema.
