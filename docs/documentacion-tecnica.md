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

**Notas importantes para la defensa:**
- Cada servicio se registra en Eureka usando su IP interna de Render (no su hostname), porque
  el hostname del contenedor no es resoluble por los demás servicios — esto está configurado en
  `application-render.yml` de cada servicio (`eureka.instance.prefer-ip-address: true`). Si el
  profesor pregunta por qué, esa es la explicación técnica.
- Las variables de entorno como `DB_HOST`, `EUREKA_HOST`, `BRANCH_HOST`, etc. no están
  hardcodeadas en ningún archivo: `render.yaml` las genera dinámicamente en el momento del
  deploy usando `fromService` (para apuntar a otro servicio del mismo Blueprint) y
  `fromDatabase` (para apuntar a la base Postgres). Esto es lo que hace posible que los 12
  servicios se conecten entre sí sin que nadie escriba una URL a mano.
