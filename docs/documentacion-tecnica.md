# Documentación técnica — GymFlow

## Stack

- Java 17
- Spring Boot 3.2.5 / Spring Cloud 2023.0.1
- Spring Cloud Netflix Eureka (service discovery)
- Spring Cloud Gateway (enrutamiento)
- Spring Data JPA + Flyway (migraciones versionadas)
- H2 (8 servicios, datos operativos) / PostgreSQL (4 servicios, datos críticos)
- Spring Security + BCrypt (autenticación/autorización en `user-service`)
- OpenFeign + Spring `RestClient` (comunicación entre servicios)
- springdoc-openapi (Swagger UI)
- JUnit 5 + Mockito (pruebas unitarias)
- Docker + Docker Compose (local) / Render (remoto)

## Nota: los 4 servicios con Postgres comparten instancia, no schema

El plan free de Render solo permite una base de datos Postgres. Los 4 servicios con datos
críticos (`user`, `branch`, `membership`, `access`) comparten esa misma instancia, pero cada
uno tiene su **propio schema** (`user_service`, `branch_service`, `membership_service`,
`access_service`), configurado en su `application-render.yml`
(`spring.flyway.schemas` + `spring.jpa.properties.hibernate.default_schema`). Sin esto, los 4
servicios escribirían su historial de migraciones Flyway en la misma tabla
`public.flyway_schema_history`, y Flyway confundiría la migración `V1` de un servicio con la
`V1` de otro (mismo número de versión, contenido distinto → error de checksum). Cada schema
mantiene su propio historial de migraciones y sus propias tablas, aislado de los demás, aunque
técnicamente vivan en la misma base de datos física.

## Patrones aplicados

**Controller → Service → Repository (CSR).** Cada servicio de dominio separa: `controller`
(recibe HTTP, valida con `@Valid`, no tiene lógica de negocio), `service` (reglas de negocio,
lanza excepciones de dominio como `EntityNotFoundException`/`IllegalStateException`), y
`repository` (Spring Data JPA, sin lógica).

**DTOs separados de entidades.** Ningún controller recibe ni devuelve una entidad `@Entity`
directamente. Cada servicio tiene `dto/<Nombre>Request.java` (con Bean Validation) y
`dto/<Nombre>Response.java`.

**Manejo de errores centralizado.** Cada servicio tiene `config/GlobalExceptionHandler.java`
(`@ControllerAdvice`) que traduce excepciones a un `ErrorResponse` con la misma forma en los 12
servicios: `timestamp`, `status`, `error`, `message`, `path`, y opcionalmente `fields` (errores
de validación campo por campo).

**Comunicación entre servicios — dos formas, a propósito:**
- **Feign** (`@FeignClient`) cuando la llamada es simple y el servicio de destino está bien
  identificado por nombre (Eureka) o por URL fija.
- **`RestClient`** (no `RestTemplate`, que está en modo mantenimiento; no `WebClient`, que es
  reactivo y este proyecto es bloqueante) cuando se quiere control explícito de timeouts y
  manejo de errores de red, usado en `class-service → membership-service`.

**Seguridad real, no decorativa.** `user-service` usa `SecurityFilterChain` con
`sessionCreationPolicy: STATELESS`, `httpBasic` con `AuthenticationEntryPoint` propio (401 en
JSON) y `AccessDeniedHandler` propio (403 en JSON) — necesarios porque Spring Security lanza
estas excepciones ANTES de que el `@ControllerAdvice` pueda interceptarlas, así que se manejan
en la capa de seguridad directamente para mantener el mismo formato de error en todo el sistema.

## Responsabilidades por servicio

| Servicio | Responsabilidad | Base de datos |
|---|---|---|
| eureka-server | Registro y descubrimiento dinámico de todos los demás servicios | — |
| gateway-service | Punto de entrada único, enrutamiento por prefijo de path, header `X-Request-Id` | — |
| user-service | Usuarios, autenticación (BCrypt), autorización por rol (SOCIO/ADMIN) | Postgres |
| branch-service | Sucursales del gimnasio | Postgres |
| membership-service | Planes y membresías, cálculo de vigencia | Postgres |
| access-service | Emisión y validación de tokens de acceso físico | Postgres |
| qr-generator-service | Generación de la imagen QR (Base64) a partir de un token | H2 |
| capacity-service | Contador de aforo en tiempo real por sucursal | H2 |
| class-service | Reserva de clases, validación de membresía activa | H2 |
| routine-service | Rutinas de entrenamiento asignadas a socios | H2 |
| equipment-service | Inventario de equipos y su estado | H2 |
| notification-service | Registro de notificaciones enviadas | H2 |

## Modelo de datos y relaciones principales

GymFlow es un ecosistema de microservicios con **base de datos por servicio** (cada uno es
dueño de sus propias tablas); no hay foreign keys físicas entre servicios distintos — las
relaciones entre entidades de servicios distintos son **lógicas**, por `id` referenciado, y se
verifican en tiempo de ejecución vía Feign/RestClient, no a nivel de base de datos.

**Entidades principales y sus campos clave:**

- `users` (user-service): `id, name, email (unique), subscriptionPlan, password, role, branchId`
- `branches` (branch-service): `id, name, address, maxCapacity`
- `memberships` (membership-service): `id, userId, planId, fechaInicio, fechaVencimiento, estado`
- `plans` (membership-service): `id, nombre, precio, duracionDias, descripcion` (catálogo fijo,
  sembrado por Flyway — no hay endpoint para crear planes vía API)
- `access_tokens` (access-service): `id, userId, branchId, qrCode, fechaExpiracion, estado`
- `access_logs` (access-service): `id, userId, branchId, tipo, timestamp`

**Relaciones lógicas (cross-servicio, verificadas en tiempo de ejecución, no por FK física):**

- `users.branchId` → `branches.id`: verificado por Feign (`BranchClient.getBranchById`) al
  crear un usuario; si la sucursal no existe, se rechaza con 404 antes de guardar.
- `memberships.userId` → `users.id`: no se verifica activamente (no hay Feign hacia
  `user-service` desde `membership-service`); se asume un `userId` válido provisto por quien
  llama.
- `memberships.planId` → `plans.id`: relación real dentro del mismo servicio (sí es FK física,
  ambas tablas viven en `membership-service`); si el plan no existe, 404.
- `access_tokens.userId` → `users.id` y verificación de membresía activa: `access-service`
  consulta `membership-service` (Feign) por `userId` antes de emitir el token.
- `access_logs` se genera a partir de un `access_tokens` validado (mismo servicio, misma base).

## Logs

Cada servicio usa SLF4J (vía Spring Boot) con niveles diferenciados: `debug` para flujo normal
detallado (ej. "Validando sucursal #1 contra branch-service"), `warn` para rechazos de negocio
esperados (ej. "Registro rechazado: la sucursal no existe"), `error` para fallos no esperados
(ej. dependencia remota caída). En local, los logs se ven directo en la terminal donde corre
`mvnw spring-boot:run` o con `docker compose logs -f <servicio>`. En Render, cada servicio tiene
su propia pestaña **Logs** en el dashboard, con historial y logs en vivo — usado activamente
durante el despliegue de esta entrega para diagnosticar el `UnknownHostException` de Eureka y el
`Migration checksum mismatch` de Flyway (ver `plan-cierre-feedback.md`).

## Pruebas

42 tests unitarios (JUnit 5 + Mockito) distribuidos en los 10 servicios de dominio, todos en
`src/test/java`, siguiendo la estructura Given-When-Then. Se prueba la capa `service` (donde
vive la lógica de negocio), incluyendo casos de éxito y casos de error (entidad no encontrada,
datos inválidos, dependencia remota caída). No hay pruebas de integración (`@SpringBootTest`)
ni de `Controller`/`Repository` por separado — se priorizó cubrir las reglas de negocio, que es
lo que pedía el feedback de la 3ª evaluación (`plan-cierre-feedback.md`, FB-03). El detalle
completo de qué prueba cada test está en `docs/matriz-requerimientos.md` (columna "Prueba
asociada"). Ejecutar con `mvnw test` desde cada servicio; ver comando exacto en el README.

## Estructura del repositorio

```
gymflow-ecosystem/
├── render.yaml                  # Blueprint de despliegue en Render (12 servicios + Postgres)
├── .env.example                 # Referencia de variables de entorno (no contiene secretos)
├── docker-compose.yml           # Levanta los 12 servicios localmente
├── docs/
│   ├── README.md
│   ├── checklist-diagnostico.md
│   ├── matriz-requerimientos.md
│   ├── plan-cierre-feedback.md
│   ├── documentacion-funcional.md
│   ├── documentacion-tecnica.md         (este archivo)
│   ├── levantamiento-requerimientos-actualizado.md
│   ├── gymflow.http
│   ├── presentacion-defensa-grupal.md
│   └── defensa-individual/
│       └── sepulveda-eduardo.md
├── eureka-server/
├── gateway-service/
├── user-service/
├── branch-service/
├── membership-service/
├── access-service/
├── qr-generator-service/
├── capacity-service/
├── class-service/
├── routine-service/
├── equipment-service/
└── notification-service/
    (cada carpeta de servicio sigue la misma estructura interna, ver README.md raíz)
```

## Ejecución desde cero

### Opción A: local con Docker Compose (recomendada para probar antes de la defensa)

Requisitos: Docker Desktop instalado y corriendo.

```bash
cd gymflow-ecosystem
docker compose up --build
```

Espera a que los 12 contenedores terminen de levantar (1-2 minutos). Verifica:
- `http://localhost:8761` → dashboard de Eureka, deberías ver los 12 servicios registrados
  (`INSTANCES CURRENTLY REGISTERED WITH EUREKA`).
- `http://localhost:8080/swagger-ui.html` → Swagger de `user-service` (repite con cada puerto
  de la tabla del README para el resto).
- Prueba los endpoints con `docs/gymflow.http`.

Para bajar todo: `docker compose down`.

### Opción B: despliegue remoto en Render

**Paso 1 — Crear la cuenta (lo hace Eduardo, no la IA).**
1. Ir a `https://dashboard.render.com/register`.
2. Registrarse (recomendado: con la cuenta de GitHub donde está este repositorio, simplifica el
   paso 2).

**Paso 2 — Conectar el repositorio y desplegar el Blueprint.**
1. En el dashboard de Render: **New +** → **Blueprint**.
2. Seleccionar el repositorio de GitHub de GymFlow (Render pide autorización de acceso la
   primera vez).
3. Render detecta automáticamente el archivo `render.yaml` en la raíz del repo y muestra un
   preview con los 13 recursos que va a crear (12 servicios web + 1 base Postgres).
4. Revisar el preview y hacer clic en **Apply** (o **Create New Resources**, el texto exacto
   puede variar levemente según la versión del dashboard).
5. Render empieza a construir las 12 imágenes Docker en paralelo. Cada build tarda entre 3 y 8
   minutos (Maven descarga dependencias en cada build la primera vez). Con 12 servicios, la
   primera vez puede tomar 15-30 minutos en total — no es un error, es el tiempo normal de
   build en el plan gratuito.
6. Cuando todos los servicios queden en estado **Live** (verde), verificar en el dashboard de
   `eureka-server` (buscar su URL pública en el dashboard, sección **Connect**) que los otros
   11 servicios aparezcan registrados.

**Paso 3 — Verificar la base de datos.**
1. En el dashboard, entrar al recurso `gymflow-postgres`.
2. Anotar la fecha de expiración del plan free (Render la muestra en la página del recurso) y
   agendar la defensa antes de esa fecha, o hacer upgrade si es necesario.

**Paso 4 — Conectar los servicios entre sí (paso manual, una sola vez).**

El plan free de Render permite que un servicio **envíe** tráfico por la red privada, pero no que
lo **reciba**. Como los 12 servicios están en el plan free, no pueden usar la red privada entre
ellos (se intentó primero con `fromService`/hostnames internos y falló con
`UnknownHostException` — ver `plan-cierre-feedback.md` si el profesor pregunta por el proceso).
La solución: cada servicio habla con los demás por su **URL pública** (`https://...onrender.com`),
que sí funciona en ambas direcciones incluso en el plan free. Render solo conoce esa URL después
de crear el servicio (lleva un sufijo aleatorio), así que hay que copiarla a mano una vez:

1. Entra a **eureka-server** en el dashboard y copia su URL pública.
2. Para cada uno de los otros 11 servicios: entra a **Environment** → agrega la variable
   `EUREKA_SERVER_URL` con el valor `https://<url-de-eureka-server>/eureka/` (con el `/eureka/`
   al final).
3. Copia también la URL pública de **branch-service** y agrégala como `BRANCH_SERVICE_URL`
   (sin el `/eureka/`) únicamente en **user-service**.
4. Copia la URL pública de **membership-service** y agrégala como `MEMBERSHIP_SERVICE_URL`
   únicamente en **class-service**.
5. Cada servicio se redespliega solo apenas guardas la variable nueva.

**Notas importantes para la defensa:**
- Cada servicio se registra en Eureka anunciando su **URL pública** (no su IP ni hostname
  privado), vía `eureka.instance.hostname: ${RENDER_EXTERNAL_HOSTNAME}` y
  `secure-port-enabled: true` / `secure-port: 443` en `application-render.yml`. Así, cuando el
  Gateway o un Feign client resuelven un servicio por nombre a través de Eureka, la URL que
  reciben es pública y viaja por internet normal, no por la red privada restringida del plan
  free. `RENDER_EXTERNAL_HOSTNAME` es una variable que Render inyecta automáticamente en cada
  servicio (no hay que configurarla a mano).
- Las variables `DB_HOST`, `DB_PORT`, etc. (conexión a Postgres) sí se generan automáticamente
  vía `fromDatabase` en `render.yaml` — esa conexión no pasa por la red privada entre servicios,
  así que no tiene la misma restricción.
