# GymFlow — Ecosistema de microservicios

Proyecto EFT DSY1103 (Full Stack 1, Duoc UC). Sistema de gestión de gimnasio construido como
12 microservicios independientes con Spring Boot 3.2.5, Spring Cloud 2023.0.1 y Java 17.

## Equipo

- Eduardo Sepulveda
- Joaquín Sandoval

Coordinación del equipo: Discord y reuniones presenciales (sin tablero Kanban/Trello formal).

## Problema

Un gimnasio con varias sucursales necesita controlar en un solo sistema: quiénes son sus
socios y si su membresía sigue vigente, el acceso físico a cada sucursal (sin depender de un
guardia que reconozca caras), el aforo en tiempo real, la reserva de clases, el seguimiento de
rutinas de entrenamiento, el estado del equipamiento y el envío de notificaciones — todo esto
sin que la caída de un módulo (por ejemplo, notificaciones) bloquee lo demás (por ejemplo, el
control de acceso).

## Solución

GymFlow separa cada responsabilidad en un microservicio independiente (12 en total, más un
Gateway y un servidor de descubrimiento), que se comunican entre sí por HTTP (Feign/RestClient)
y se registran dinámicamente en Eureka. Esto permite que cada servicio se despliegue, escale y
falle de forma aislada: si `notification-service` se cae, `access-service` sigue validando
entradas con normalidad (ver "Comunicación entre servicios" más abajo, especialmente el caso
`access-service → capacity-service`, tolerante a fallas).

## Arquitectura

```
                                   ┌─────────────────┐
                                   │  eureka-server   │  :8761
                                   │ (service registry)│
                                   └────────▲─────────┘
                                            │ registro
                    ┌───────────────────────┼───────────────────────┐
                    │                       │                       │
             ┌──────▼──────┐         ┌──────▼──────┐         ┌──────▼──────┐
Cliente ───▶ │   gateway    │  lb://  │ 10 servicios │  Feign/ │             │
             │  :8082       │────────▶│ de dominio   │◀────────│ RestClient  │
             └─────────────┘         └─────────────┘         └─────────────┘
```

Todos los servicios se registran en `eureka-server` y el `gateway-service` enruta con
`lb://<nombre-servicio>` (balanceo vía Eureka, no URLs fijas). El Gateway agrega un header
`X-Request-Id` a cada request entrante (o lo respeta si ya viene) para trazabilidad entre servicios.

## Servicios y puertos

| Servicio | Puerto | Base de datos | Rol | URL pública (Render) |
|---|---|---|---|---|
| eureka-server | 8761 | — | Registro de servicios | https://eureka-server-x49r.onrender.com |
| gateway-service | 8082 | — | Punto de entrada único, enrutamiento `lb://` | https://gateway-service-yitx.onrender.com |
| user-service | 8080 | Postgres | Usuarios, autenticación (Basic Auth + BCrypt), roles SOCIO/ADMIN | https://user-service-jny5.onrender.com |
| branch-service | 8081 | Postgres | Sucursales | https://branch-service-u39g.onrender.com |
| membership-service | 8083 | Postgres | Membresías y su estado (activa/inactiva) | https://membership-service-7po0.onrender.com |
| access-service | 8084 | Postgres | Control de acceso (valida QR + membresía activa) | https://access-service-3da4.onrender.com |
| qr-generator-service | 8085 | H2 (memoria) | Generación de códigos QR | https://qr-generator-service-p4wn.onrender.com |
| capacity-service | 8086 | H2 (memoria) | Aforo en tiempo real por sucursal | https://capacity-service.onrender.com |
| class-service | 8087 | H2 (memoria) | Reserva de clases (valida membresía activa vía RestClient) | https://class-service-xexo.onrender.com |
| routine-service | 8088 | H2 (memoria) | Rutinas de entrenamiento | https://routine-service-0snh.onrender.com |
| equipment-service | 8089 | H2 (memoria) | Inventario de equipos | https://equipment-service-472j.onrender.com |
| notification-service | 8090 | H2 (memoria) | Notificaciones (EMAIL/SMS/PUSH) | https://notification-service-mgoz.onrender.com |

Nota: en el plan free de Render los servicios se "duermen" tras 15 minutos sin tráfico; la
primera petición después de eso puede tardar hasta 50 segundos en responder mientras despierta.

Los 4 servicios con datos de negocio críticos (usuarios, sucursales, membresías, accesos) usan
Postgres real en Render. Los otros 8 manejan datos operativos/efímeros y se dejan en H2 en
memoria — justificación completa en `docs/documentacion-tecnica.md`.

## Comunicación entre servicios

- **Feign** (`@FeignClient`): `user-service → branch-service` (URL fija), `access-service →
  capacity-service` y `access-service → membership-service` (vía nombre + Eureka).
- **RestClient** (Spring 3.2+, no `RestTemplate`): `class-service → membership-service`, con
  timeout de conexión/lectura de 3s y manejo de error si el servicio remoto no responde.

## Ejecutar todo localmente (Docker Compose)

Requisitos: Docker y Docker Compose instalados.

```bash
docker compose up --build
```

Esto levanta los 12 servicios en la red `gymflow-net`. `eureka-server` queda disponible en
`http://localhost:8761` (dashboard de instancias registradas). El resto de servicios expone
Swagger UI en `http://localhost:<puerto>/swagger-ui.html`.

## Desplegar en Render

Ver `docs/documentacion-tecnica.md` → sección "Ejecución desde cero" para la guía paso a paso
completa (creación de cuenta, Blueprint, variables de entorno).

Resumen: el archivo `render.yaml` en la raíz define los 12 servicios + 1 base Postgres. Desde
el dashboard de Render: **New +** → **Blueprint** → conectar este repositorio → Render crea
todo automáticamente.

## Estructura del repositorio

```
gymflow-ecosystem/
├── README.md
├── render.yaml                  # Blueprint de despliegue en Render (12 servicios + Postgres)
├── .env.example                 # Referencia de variables de entorno (sin secretos reales)
├── .gitignore
├── docker-compose.yml           # Levanta los 12 servicios localmente
├── start-all.cmd                # Atajo Windows: arranca los 12 servicios en orden con mvnw
├── docs/
│   ├── matriz-requerimientos.md
│   ├── plan-cierre-feedback.md
│   ├── documentacion-funcional.md
│   ├── documentacion-tecnica.md
│   ├── levantamiento-requerimientos-actualizado.md
│   ├── presentacion-defensa-grupal.pptx
│   ├── gymflow.http
│   └── defensa-individual/
│       ├── sepulveda-eduardo.md
│       └── sandoval-joaquin.md
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
    (cada carpeta de servicio sigue la estructura de la sección siguiente)
```

## Orden de arranque de servicios

Importa solo para ejecución **local** sin Docker Compose (Docker Compose ya maneja el orden por
healthchecks). Arranca en este orden, esperando ~15-20s entre cada paso para que cada uno
termine de registrarse en Eureka:

1. `eureka-server` (todo lo demás depende de que esté arriba primero)
2. `branch-service` y `membership-service` (no dependen de otros servicios de negocio)
3. `user-service` (depende de `branch-service` vía Feign) y el resto de servicios de dominio
   (`access-service`, `qr-generator-service`, `capacity-service`, `class-service`,
   `routine-service`, `equipment-service`, `notification-service`)
4. `gateway-service` (al final, para que ya tenga servicios registrados a los cuales enrutar)

## Comandos para ejecutar cada servicio sin IDE

Desde la carpeta de cada servicio (Windows usa `mvnw.cmd`, Linux/macOS usa `./mvnw`):

```bash
cd eureka-server        && ./mvnw spring-boot:run     # o mvnw.cmd spring-boot:run
cd branch-service       && ./mvnw spring-boot:run
cd membership-service   && ./mvnw spring-boot:run
cd user-service         && ./mvnw spring-boot:run
cd access-service       && ./mvnw spring-boot:run
cd qr-generator-service && ./mvnw spring-boot:run
cd capacity-service     && ./mvnw spring-boot:run
cd class-service        && ./mvnw spring-boot:run
cd routine-service      && ./mvnw spring-boot:run
cd equipment-service    && ./mvnw spring-boot:run
cd notification-service && ./mvnw spring-boot:run
cd gateway-service      && ./mvnw spring-boot:run
```

## Comandos para correr las pruebas

```bash
cd <cualquier-servicio-de-dominio>
./mvnw test          # Linux/macOS
mvnw.cmd test         # Windows
```

Corre los 42 tests unitarios (JUnit 5 + Mockito) de ese servicio. Repetir en cada uno de los 10
servicios de dominio para correr la suite completa.

## Rutas principales del Gateway

Todo pasa por `http://localhost:8082` (local) o la URL pública de `gateway-service` (Render).
El Gateway enruta por prefijo de path, no por nombre de servicio:

| Prefijo | Servicio destino |
|---|---|
| `/api/users/**` | user-service |
| `/api/branches/**` | branch-service |
| `/api/membership/**` | membership-service |
| `/api/access/**` | access-service |
| `/api/qr/**` | qr-generator-service |
| `/api/capacity/**` | capacity-service |
| `/api/classes/**` | class-service |
| `/api/routines/**` | routine-service |
| `/api/equipment/**` | equipment-service |
| `/api/notify/**` | notification-service |

## Usuarios de prueba y roles

Sembrados vía Flyway (`V4__seed_users.sql` en `user-service`), contraseñas reales con BCrypt:

| Email | Contraseña | Rol |
|---|---|---|
| `admin@gymflow.cl` | `admin123` | ADMIN (puede listar todos los usuarios, `GET /api/users`) |
| `socio@gymflow.cl` | `socio123` | SOCIO (rol estándar, sin acceso a endpoints de ADMIN) |

Autenticación vía Basic Auth. Ejemplo de header ya codificado en `docs/gymflow.http`.

## Estructura de cada microservicio

```
<servicio>/
├── Dockerfile
├── pom.xml
└── src/main/
    ├── java/.../
    │   ├── controller/    # expone endpoints REST, valida con @Valid
    │   ├── service/       # lógica de negocio
    │   ├── repository/    # acceso a datos (Spring Data JPA)
    │   ├── model/          # entidades JPA
    │   ├── dto/            # objetos de entrada/salida (nunca se expone la entidad directo)
    │   ├── client/          # Feign/RestClient hacia otros servicios (si aplica)
    │   └── config/          # GlobalExceptionHandler, OpenApiConfig, seguridad (si aplica)
    └── resources/
        ├── application.yaml         # config base (perfil default, H2 o Postgres local)
        ├── application-render.yml   # overrides para desplegar en Render
        └── db/migration/            # scripts Flyway versionados (V1, V2, ...)
```

## Documentación completa

Ver carpeta `docs/`:
- `matriz-requerimientos.md` — cumplimiento de cada requisito de la pauta.
- `plan-cierre-feedback.md` — cómo se resolvió cada observación previa del profesor.
- `documentacion-funcional.md` — qué hace el sistema, flujos de usuario.
- `documentacion-tecnica.md` — arquitectura, patrones, cómo correr todo desde cero.
- `levantamiento-requerimientos-actualizado.md` — requerimientos actualizados a la entrega final.
- `defensa-individual/sepulveda-eduardo.md` — guía de defensa personal (Eduardo).
- `defensa-individual/sandoval-joaquin.md` — guía de defensa personal (Joaquín).
- `gymflow.http` — colección de requests de ejemplo para probar cada endpoint.
