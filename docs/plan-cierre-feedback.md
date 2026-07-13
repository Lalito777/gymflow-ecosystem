# Plan de cierre de feedback del profesor

Este documento responde punto por punto a las observaciones que el profesor había marcado en
la entrega previa, más los hallazgos adicionales encontrados durante el diagnóstico propio
(que eran más profundos que lo que el profesor alcanzó a revisar).

## 1. Observaciones explícitas del profesor

| Observación del profesor | Causa raíz encontrada | Solución aplicada | Dónde se ve |
|---|---|---|---|
| `equipment-service` no tiene capa `service` | El `controller` llamaba directo al `repository` | Se creó `service/EquipmentService.java` con la regla de negocio (estados válidos: `DISPONIBLE`, `EN_MANTENCION`, `FUERA_DE_SERVICIO`) y el `controller` ahora depende de él | `equipment-service/src/main/java/.../service/EquipmentService.java` |
| `notification-service` no tiene capa `service` | Igual que el anterior | Se creó `service/NotificationService.java` con validación de tipo (`EMAIL`, `SMS`, `PUSH`) | `notification-service/.../service/NotificationService.java` |
| Llamada Feign `user→branch` no se usa realmente | `BranchClient` estaba declarado con `@FeignClient` pero nunca se inyectaba en ningún servicio — código muerto | `UserService` ahora inyecta `BranchClient` y llama `verifyBranchExists()` antes de crear un usuario; si la sucursal no existe lanza 404, si branch-service no responde lanza 503 | `user-service/.../service/UserService.java` |

## 2. Hallazgos adicionales (no marcados antes, pero exigidos por la pauta)

Al revisar el código real en vez de asumir el estado del proyecto, aparecieron huecos más
grandes que lo que el profesor alcanzó a señalar:

- **8 de 10 servicios de dominio no tenían DTOs**: los controllers recibían `Map<String, Long>`
  como body o devolvían directamente la entidad JPA. Esto rompe el requisito explícito de la
  pauta ("separación entre DTOs y entidades") y toca ítems de rúbrica sobre validación y
  modelado. Se corrigió en los 8 servicios (`membership`, `access`, `qr-generator`, `capacity`,
  `class`, `routine`, `equipment`, `notification`).
- **Cero Bean Validation en todo el proyecto**: ningún servicio validaba datos de entrada con
  `@NotNull`/`@Positive`/etc. Se agregó en los 10 DTOs de request.
- **Seguridad decorativa**: `user-service` tenía BCrypt configurado pero `SecurityConfig` usaba
  `.anyRequest().permitAll()`, es decir, cualquiera podía llamar cualquier endpoint sin
  autenticarse. Se reemplazó por reglas reales por rol.
- **`import.sql` de `user-service` nunca se ejecutaba**: el proyecto usa `ddl-auto: validate`,
  que ignora `import.sql`. Los usuarios de prueba (admin/socio) se movieron a una migración
  Flyway real (`V4__seed_users.sql`) con contraseñas hasheadas con BCrypt.
- **Cero comunicación con Spring `RestClient`**: la pauta pide al menos una integración con
  `RestClient`/`WebClient` además de Feign. Se implementó `class-service → membership-service`.
- **Cero anotaciones Swagger**: `springdoc` estaba en el `pom.xml` pero sin `@Operation`ni
  `@ApiResponse`, por lo que Swagger UI mostraba endpoints sin documentar. Se anotaron los 12
  servicios.
- **Sin driver Postgres ni perfil de despliegue**: los 12 servicios solo tenían H2. Se agregó
  Postgres + perfil `render` a los 4 servicios con datos críticos.

## 3. Decisión explícita y su justificación: no todos los servicios migran a Postgres

Se evaluó migrar los 12 servicios a Postgres, pero con el tiempo disponible (~4 horas) se
priorizó lo que más pesa en la defensa individual. Se migraron los 4 servicios con datos de
negocio persistentes y críticos (`user`, `branch`, `membership`, `access`). Los otros 8 manejan
datos operativos que no necesitan sobrevivir un reinicio del contenedor para efectos de la
demo (aforo, notificaciones, rutinas, inventario, reservas, QR) y se dejaron en H2 en memoria,
documentado explícitamente para poder defenderlo si el profesor pregunta por qué.
