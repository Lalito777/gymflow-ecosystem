# Plan de cierre y feedback — GymFlow

## Feedback recibido en la 3ª evaluación (texto completo del profesor)

**Fortalezas:** el ecosistema de 12 microservicios compila y se ejecuta sin errores, y se
confirmó dinámicamente que los 5 servicios con pruebas pasan la totalidad de sus casos. Las
reglas de negocio de `access-service` (validación de membresía activa, control de expiración y
reuso de QR) están bien pensadas y resueltas, y la configuración Docker Compose con registro
dinámico en Eureka está bien lograda.

**A mejorar:** `equipment-service` y `notification-service` acceden a la base de datos
directamente desde el controller, sin una capa `service` intermedia — vale la pena completarla
para mantener la arquitectura consistente en todo el proyecto. Las pruebas unitarias solo cubren
5 de 12 servicios; extenderlas al resto sería el siguiente paso natural. La documentación
Swagger existe pero es puramente autogenerada, sin ninguna anotación (`@Operation`,
`@ApiResponse`) que la enriquezca — agregarlas no toma mucho esfuerzo y mejora bastante la
experiencia de quien consume la API. En la Defensa, se menciona una comunicación Feign
(`user-service → branch-service`) que en realidad nunca se invoca en el código, y se afirma que
Swagger tiene ejemplos y descripciones que en la práctica no existen — conviene revisar bien lo
que se va a presentar contra el código antes de la exposición.

**Para la próxima:** la base funcional está bien encaminada; completar la capa de servicio en
los dos módulos que la omiten y sumar pruebas al resto del ecosistema son los pasos más
directos para subir el nivel del proyecto.

## Acciones realizadas, ítem por ítem

| ID | Observación o feedback recibido | Acción realizada | Archivo(s) modificados | Evidencia de verificación | Estado |
|---|---|---|---|---|---|
| FB-01 | `equipment-service` accede a la BD directo desde el controller, sin capa `service` | Se creó `service/EquipmentService.java` con la regla de negocio (estados válidos: `DISPONIBLE`, `EN_MANTENCION`, `FUERA_DE_SERVICIO`); el controller ahora depende de él, no del repository | `EquipmentController.java`, `EquipmentService.java` (nuevo) | `EquipmentServiceTest.create_conEstadoValido_deberiaGuardarElEquipo`, `create_conEstadoInvalido_deberiaLanzarExcepcion` — `mvnw test` BUILD SUCCESS 3/3 | Corregido |
| FB-02 | `notification-service` accede a la BD directo desde el controller, sin capa `service` | Se creó `service/NotificationService.java` con la regla de negocio (tipos válidos: `EMAIL`, `SMS`, `PUSH`) | `NotificationController.java`, `NotificationService.java` (nuevo) | `NotificationServiceTest.send_conTipoValido_deberiaGuardarLaNotificacion`, `send_conTipoInvalido_deberiaLanzarExcepcion` — `mvnw test` BUILD SUCCESS 2/2 | Corregido |
| FB-03 | Pruebas unitarias solo cubren 5 de 12 servicios (`user`, `branch`, `membership`, `access`, `qr-generator`) | Se agregaron tests nuevos en los 5 servicios que no tenían: `capacity`, `class`, `routine`, `equipment`, `notification` | `CapacityServiceTest.java`, `ClassServiceTest.java`, `RoutineServiceTest.java`, `EquipmentServiceTest.java`, `NotificationServiceTest.java` (los 5 nuevos) | `mvnw test` en los 10 servicios de dominio → 42 tests, 0 fallas | Corregido |
| FB-04 | Swagger es puramente autogenerado, sin `@Operation`/`@ApiResponse` | Se anotó cada endpoint de los 12 servicios con `@Tag`, `@Operation`, `@ApiResponses` (incluyendo el schema real de `ErrorResponse` en cada código de error), y se agregó `config/OpenApiConfig.java` por servicio | Los 12 `*Controller.java` + 12 `OpenApiConfig.java` (nuevos) | Revisión manual de `/swagger-ui.html` en cada servicio — cada endpoint muestra descripción, parámetros y códigos de respuesta reales | Corregido |
| FB-05 | La Defensa menciona una comunicación Feign `user-service → branch-service` que nunca se invoca en el código (código muerto: `BranchClient` declarado pero no inyectado en ningún lado) | `UserService` ahora inyecta `BranchClient` y llama `verifyBranchExists(branchId)` antes de guardar cada usuario nuevo; además se agregó el endpoint `GET /api/branches/{id}` en `branch-service`, que antes no existía (por eso la llamada real nunca podría haber funcionado) | `UserService.java`, `BranchController.java`, `BranchService.java` | `UserServiceTest.create_conSucursalInexistente_deberiaLanzarEntityNotFoundYNoGuardar` (simula el Feign fallando con `FeignException.NotFound` real, no un mock genérico) | Corregido |
| FB-06 | Se afirma que Swagger tiene ejemplos y descripciones que en la práctica no existen (discrepancia entre lo presentado y el código real) | No es un bug de código sino un problema de rigor en la presentación anterior. Para esta entrega, cada documento de `docs/` se escribió leyendo el código real primero (controllers, DTOs, tests) y no al revés — incluso se detectó y corrigió un error propio en `docs/gymflow.http` (campos de request que no coincidían con los DTOs reales) antes de dar la documentación por terminada | Todo `docs/` | Cada afirmación de este plan de cierre referencia un archivo y una prueba real, verificable abriendo el repositorio | Corregido (proceso) |

## Observaciones no corregidas, con justificación

- **Cobertura de pruebas más allá de la capa `service`:** no se agregaron pruebas de integración
  (`@SpringBootTest`) ni pruebas de `Controller`/`Repository` por separado — se priorizó cubrir
  la capa `service` (donde vive la lógica de negocio) en los 10 servicios de dominio, que es lo
  que pedía explícitamente el feedback. Pruebas de integración quedan como mejora futura.
- **Migración completa a Postgres:** solo 4 de los 12 servicios (`user`, `branch`, `membership`,
  `access`, los de datos críticos) se migraron a Postgres real en Render; los otros 8 siguen en
  H2 en memoria. Justificación completa en `documentacion-tecnica.md`. No fue una observación
  explícita del profesor, pero se documenta aquí porque es una decisión de alcance relevante
  para la defensa.

## Hallazgos adicionales encontrados al revisar el código (no marcados antes por el profesor)

Al revisar el código real en vez de asumir el estado del proyecto, aparecieron huecos más
grandes que lo que el profesor alcanzó a señalar en la 3ª evaluación:

- **8 de 10 servicios de dominio no tenían DTOs**: los controllers recibían `Map<String, Long>`
  como body o devolvían directamente la entidad JPA. Se corrigió en los 8 servicios
  (`membership`, `access`, `qr-generator`, `capacity`, `class`, `routine`, `equipment`,
  `notification`).
- **Cero Bean Validation en todo el proyecto**: se agregó en los 10 DTOs de request.
- **Seguridad decorativa**: `user-service` tenía BCrypt configurado pero `SecurityConfig` usaba
  `.anyRequest().permitAll()`. Se reemplazó por reglas reales por rol.
- **`import.sql` de `user-service` nunca se ejecutaba** (el proyecto usa `ddl-auto: validate`,
  que lo ignora). Los usuarios de prueba se movieron a `V4__seed_users.sql` (Flyway real, BCrypt).
- **Cero comunicación con Spring `RestClient`**: se implementó `class-service → membership-service`.
- **Sin driver Postgres ni perfil de despliegue**: se agregó Postgres + perfil `render` a los 4
  servicios con datos críticos, y se completó el despliegue real en Render (12 servicios +
  Gateway + Eureka + Postgres, con schema propio por servicio).
