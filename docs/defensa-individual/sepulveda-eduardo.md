# Defensa individual — Eduardo Sepúlveda

Guía personal para la defensa en vivo. La idea no es memorizar esto, sino entender cada punto
lo suficiente para explicarlo con tus palabras y modificar el código en vivo si te lo piden.
Recuerda: en la defensa NO puedes usar IA — este documento es para estudiar antes, no para leer
durante.

## 1. Qué cambié y por qué 

Encontré que el proyecto tenía huecos más grandes de lo que el profesor había marcado: 8 de 10
servicios exponían directamente sus entidades de base de datos en vez de usar DTOs, ningún
servicio validaba los datos que recibía, y la seguridad de `user-service` estaba configurada
para dejar pasar cualquier request sin autenticarse. Prioricé arreglar eso primero porque son
requisitos explícitos de la pauta y sin ellos varios ítems quedan en 0 directo, y después
agregué lo que el profesor sí había marcado (capa `service` faltante en 2 servicios, Feign que
nunca se llamaba), seguridad real con roles, una integración nueva con `RestClient`, pruebas
unitarias donde faltaban, documentación Swagger, y preparé todo para desplegar en Postgres +
Render.

## 2. Los cambios más importantes, explicados uno por uno

### DTOs y validación (los 10 servicios de dominio)
**Qué:** cada servicio ahora tiene una clase `XxxRequest` (lo que llega en el body, con
anotaciones como `@NotNull`, `@NotBlank`, `@Positive`) y una `XxxResponse` (lo que se devuelve).
El controller nunca más recibe ni devuelve la entidad `@Entity` directamente.
**Por qué:** exponer la entidad JPA acopla la API pública a la estructura interna de la base de
datos — si cambio una columna, rompo el contrato con el cliente. Además, sin DTOs no hay dónde
poner las anotaciones de validación.
**Cómo lo defendería:** "Si el profesor pregunta por qué no valido directo en la entidad: la
entidad representa cómo se guarda el dato, el DTO representa el contrato de la API; son
responsabilidades distintas y mezclarlas es una mala práctica reconocida (violación de
separación de capas)."

### `GlobalExceptionHandler` uniforme
**Qué:** un `@ControllerAdvice` por servicio que atrapa excepciones (`EntityNotFoundException`
→ 404, `IllegalArgumentException`/validación → 400, `IllegalStateException` → 409/503 según el
caso) y las convierte en un JSON con la misma forma en los 12 servicios.
**Por qué:** sin esto, cada servicio devuelve errores con formato distinto (o el stacktrace
crudo de Spring), lo que hace imposible para un cliente (o el Gateway) tratarlos de forma
genérica.
**Cómo lo defendería:** poder mostrar en vivo que un `POST` con un campo faltante devuelve
siempre `{timestamp, status, error, message, path, fields}` sin importar el servicio.

### Feign real `user-service → branch-service`
**Qué:** antes existía `BranchClient` con `@FeignClient` pero nunca se inyectaba en ningún
lado — código muerto que aparentaba cumplir el requisito sin cumplirlo. Ahora `UserService`
inyecta `BranchClient` y llama `verifyBranchExists(branchId)` antes de guardar un usuario
nuevo.
**Por qué:** la pauta exige comunicación Feign *invocada*, no solo declarada.
**Cómo lo defendería:** poder mostrar el código de `verifyBranchExists()` y explicar los dos
casos de error que maneja: `FeignException.NotFound` (la sucursal no existe → 404) y cualquier
otro `FeignException` (branch-service no responde → 503, porque no es culpa del usuario que el
servicio remoto esté caído).

### RestClient nuevo `class-service → membership-service`
**Qué:** antes de confirmar una reserva de clase, `class-service` le pregunta a
`membership-service` si el usuario tiene membresía activa, usando `RestClient` (no Feign).
**Por qué usar `RestClient` y no Feign otra vez:** para demostrar dominio de las dos formas que
ofrece Spring de hacer llamadas HTTP salientes, y porque `RestClient` da más control explícito
sobre timeouts (`SimpleClientHttpRequestFactory` con 3 segundos de conexión y lectura) sin
necesidad de anotaciones declarativas.
**Cómo lo defendería:** explicar qué pasa si `membership-service` no responde a tiempo: se
captura `RestClientException` y se traduce a un mensaje de negocio claro ("no fue posible
verificar la membresía"), en vez de dejar que el error técnico llegue crudo al cliente.

### Seguridad real en `user-service`
**Qué:** antes `SecurityConfig` tenía `.anyRequest().permitAll()` (cualquiera podía llamar
cualquier endpoint). Ahora: `POST /api/users` es público (cualquiera se puede registrar),
`GET /api/users` requiere rol ADMIN, y todo lo demás requiere estar autenticado. Las
contraseñas se guardan con BCrypt. 401 (no autenticado) y 403 (autenticado pero sin permiso) se
devuelven como JSON con el mismo formato que el resto de errores.
**Por qué 401 se maneja distinto a los demás errores:** Spring Security lanza sus excepciones
de autenticación/autorización ANTES de que la petición llegue al controller, por lo tanto el
`@ControllerAdvice` (que solo intercepta excepciones que salen de un controller) nunca las ve.
Por eso se configuró un `AuthenticationEntryPoint` y un `AccessDeniedHandler` propios, que
escriben el JSON manualmente con `ObjectMapper`, pero manteniendo la misma forma que el resto de
errores del sistema.
**Cómo lo defendería:** este es probablemente el punto más técnico y el que más vale la pena
tener claro, porque es fácil que pregunten "¿por qué no usaste el ControllerAdvice para esto
también?".

### Seed de usuarios movido a Flyway
**Qué:** el archivo `import.sql` tenía usuarios de prueba pero nunca se ejecutaba porque el
proyecto usa `spring.jpa.hibernate.ddl-auto: validate` (Hibernate no toca el schema, y
`import.sql` es una función de Hibernate, no de Flyway). Se creó `V4__seed_users.sql`, una
migración Flyway real, con las contraseñas ya hasheadas con BCrypt.
**Cómo lo defendería:** poder explicar la diferencia entre `import.sql` (Hibernate, se ejecuta
solo si Hibernate genera el schema) y las migraciones Flyway (se ejecutan siempre, en orden,
independiente de la configuración de Hibernate) — es una pregunta clásica de "¿por qué no
funcionaba esto antes?".

### Filtro `X-Request-Id` en el Gateway
**Qué:** un `GlobalFilter` que agrega el header `X-Request-Id` a cada request que entra por el
Gateway (o lo respeta si el cliente ya lo mandó), y lo propaga hacia los servicios de destino.
**Por qué:** permite rastrear una misma petición a través de múltiples servicios en los logs —
sin esto, si algo falla en una cadena de 3 servicios, no hay forma de correlacionar los logs.

### Postgres + Render (preparación)
**Qué:** los 4 servicios con datos críticos (`user`, `branch`, `membership`, `access`) suman el
driver de Postgres y un perfil `application-render.yml` que reemplaza H2 por la base real. Los
otros 8 se dejaron en H2 a propósito (ver `plan-cierre-feedback.md`, sección 3, para la
justificación completa si preguntan por qué no todos).
**Cómo lo defendería:** tener clara la justificación de "por qué no migré los 12" — es una
decisión de priorización de tiempo defendible, no un olvido.

## 3. Preguntas que probablemente te hagan (y cómo responderlas)

- **"¿Por qué separaste DTOs de entidades si al final tienen casi los mismos campos?"** — Hoy
  se ven parecidos, pero son cosas distintas: uno describe el contrato de la API, el otro cómo
  se persiste el dato. Si mañana agrego un campo interno a la entidad (por ejemplo, una
  auditoría), no quiero que aparezca automáticamente en la respuesta de la API.
- **"¿Qué pasa si `branch-service` está caído cuando alguien crea un usuario?"** — `UserService`
  captura la excepción de Feign y responde 503 con un mensaje claro, en vez de que el error
  técnico se propague sin control.
- **"¿Por qué `class-service` usa RestClient y no Feign como el resto?"** — Decisión para
  mostrar ambas formas de comunicación que pide la pauta; ambas son válidas, la diferencia es
  que RestClient da más control imperativo y Feign es más declarativo/rápido de escribir.
- **"Modifica el endpoint de crear membresía para que también valide que `userId` sea
  positivo."** — Sabrías ir a `dto/MembershipRequest.java` y agregar `@Positive` sobre el campo
  `userId`, y explicar que Bean Validation se dispara automáticamente porque el controller ya
  tiene `@Valid` en la firma del método.
- **"¿Por qué no todos los servicios usan Postgres?"** — Ver la justificación en
  `plan-cierre-feedback.md`.

## 4. Si te piden modificar algo en vivo

Los lugares más probables donde te pueden pedir un cambio en vivo, y qué archivo tocarías:
- Agregar un campo/validación nueva → el DTO `Request` correspondiente (`dto/`).
- Cambiar una regla de negocio (ej. "el aforo no puede superar 100") → la clase `service`
  correspondiente.
- Agregar un endpoint nuevo → `controller` (delgado, delega a `service`) + método nuevo en
  `service` + (si aplica) método nuevo en `repository`.
- Cambiar el formato de un error → `config/GlobalExceptionHandler.java` del servicio.
- Restringir un endpoint a un rol → `config/SecurityConfig.java` (solo existe en
  `user-service`).

## 4.5. Una relación de base de datos que domino

En `membership-service`, `memberships.planId` es una foreign key real hacia `plans.id` — ambas
tablas viven en el mismo servicio y la misma base, así que ahí sí es una relación física, no
solo lógica. `plans` es un catálogo fijo (BASICO, PREMIUM, VIP) sembrado por Flyway
(`V2__seed_plans.sql`); no existe ningún endpoint para crear planes vía API a propósito, porque
son datos de catálogo del negocio, no algo que un usuario deba poder generar. Cuando se crea una
membresía (`POST /api/membership`), `MembershipService` busca el plan por `planId` con
`planRepository.findById(...).orElseThrow(...)`; si no existe, lanza `EntityNotFoundException`
(404) antes de guardar nada. Si existe, calcula `fechaVencimiento` sumando
`plan.getDuracionDias()` a la fecha actual — por eso la duración de la membresía depende
directamente del plan elegido, no es un valor fijo. Distinto es el caso de
`memberships.userId` → `users.id`: esa relación es solo lógica (cruza de `membership-service` a
`user-service`, dos bases de datos distintas), y a propósito no se verifica con un Feign en este
punto — se explica y se justifica la decisión en `documentacion-tecnica.md`, sección "Modelo de
datos y relaciones principales".

## 5. Rol dentro del equipo

Desarrollo backend transversal: estructura inicial de los 12 microservicios, capa de seguridad
de `user-service`, comunicación entre servicios (Feign + RestClient), corrección de los 6 gaps
del feedback de la 3ª evaluación, migración a Postgres y despliegue completo en Render.

## 6. Commits propios (hash real, verificable con `git log`)

| Hash | Fecha | Mensaje |
|---|---|---|
| `1e55687` | 2026-05-27 | feat: estructura inicial del ecosistema GymFlow con 10 microservicios |
| `a43d7b1` | 2026-06-28 | feat: agregar API Gateway con rutas a los 10 microservicios |
| `dd93a14` | 2026-06-28 | feat: agregar Swagger/OpenAPI y spring-boot-starter-test a los 10 servicios |
| `87df134` | 2026-06-28 | test: agregar tests unitarios JUnit+Mockito en user, branch, membership, access y qr-generator-service |
| `5939995` | 2026-06-28 | docs: actualizar README con arquitectura, puertos y guía de ejecución |
| `e7e05dd` | 2026-06-28 | fix: agregar config eureka explícita en branch-service |
| `061ea43` | 2026-06-29 | docs: agregar nombres del equipo al README |
| `72e3cde` | 2026-07-13 | Postgres + Render + documentación final |
| `ce571c9` | 2026-07-13 | fix: schema propio por servicio en Postgres compartido (Flyway checksum) |
| `06a9e36` | 2026-07-13 | fix: registrar servicios en Eureka con URL pública (plan free no recibe tráfico privado) |
| `e6f0252` | 2026-07-13 | fix: leer BRANCH_SERVICE_URL/MEMBERSHIP_SERVICE_URL directo desde el código, sin YAML intermedio |
| `ca22adb` | 2026-07-13 | docs: matriz de requerimientos y plan de cierre según pauta |

Repositorio: `https://github.com/Lalito777/gymflow-ecosystem`.

## 7. Tareas del tablero

El equipo no usó un tablero Kanban formal (Trello/GitHub Projects); la coordinación de tareas
fue por Discord y reuniones presenciales.

## 8. Checklist personal de evidencia para la defensa

- [ ] Puedo explicar por qué cada servicio usa H2 o Postgres.
- [ ] Puedo mostrar en vivo el flujo de 3 pasos de control de acceso (`generate` → `qr/create` →
      `validate`) usando `docs/gymflow.http`.
- [ ] Puedo mostrar el registro en Eureka de los 12 servicios desplegados en Render.
- [ ] Puedo explicar la limitación de red privada del plan free de Render y cómo se resolvió.
- [ ] Puedo correr `mvnw test` en al menos 2 servicios en vivo y explicar qué prueba cada test.
- [ ] Puedo señalar dónde se corrigió cada uno de los 6 puntos del feedback de la 3ª evaluación.
- [ ] Puedo modificar en vivo una regla de negocio simple sin ayuda externa.
