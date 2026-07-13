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
