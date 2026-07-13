# GymFlow — Diagnóstico y checklist EFT DSY1103 (estado real, 12 jul 2026)

> **Actualización 13 jul 2026:** Bloques 2 y 3 del plan completos y verificados con `mvnw test` en los 10 servicios de dominio (42 tests, 0 fallas). Detalle: DTOs+validación+`@ControllerAdvice` uniforme en los 10 servicios; capa `service` agregada en `equipment-service` y `notification-service`; Feign real `user→branch` (antes muerto); RestClient real nuevo `class-service→membership-service`; seguridad real en `user-service` (roles SOCIO/ADMIN, 401/403 con JSON uniforme, bug de rol `ROLE_ADMIN` y password en texto plano corregidos, seed movido a Flyway `V4__seed_users.sql`); filtro `X-Request-Id` en el Gateway; Swagger anotado (`@Operation`/`@ApiResponse`) en los 12. Pendiente: Postgres + Render (bloqueado en cuenta de Render), documentación final (`README`, `docs/`).

Este documento es la base de trabajo para las ~4 horas disponibles (2 hoy + 2 mañana). Se construyó leyendo el código real de los 12 servicios, no supuestos. Todo lo marcado ❌ es evidencia directa de grep/lectura de archivos, no inferencia.

## 0. Corrección a supuestos previos

- El paquete se llama `repository` (bien escrito) en los 10 servicios de dominio. No existe la grafía `respository` en ningún lugar del repo. Se usará `repository`.
- La pauta oficial (PDF EFT) no obliga expresamente Render para los 12 servicios — dice "al menos un entorno remoto... cuando sea parte del alcance definido por el docente". Quedó confirmado contigo que sí es exigencia real del profesor para los 12. Se mantiene como objetivo, marcado como el punto de mayor riesgo de tiempo (ver sección 4).

## 1. Hallazgo más grande no reportado antes: faltan DTOs en 8 de 10 servicios de dominio

Solo `user-service` y `branch-service` tienen paquete `dto`. Los otros 8 (`membership`, `access`, `qr-generator`, `capacity`, `class`, `routine`, `equipment`, `notification`) **exponen la entidad JPA directamente en el controller** y reciben `Map<String, Long>` o el `@Entity` como request body (confirmado en `MembershipController`: `create(@RequestBody Map<String, Long> body)` devuelve `Membership` la entidad).

Esto es un requisito explícito de la pauta oficial ("Separación entre DTOs y entidades para validar datos de manera limpia y segura") y hoy no se cumple en el 80% de los servicios. Es más grave que lo que marcó el profesor la vez pasada porque toca directamente 3 ítems de la rúbrica: IE 1.3.1 (validaciones, 3%), IE 2.1.1 (modelado/CRUD, 4%) y el ítem crítico "recurso declarado no implementado = 0".

## 2. Matriz de cumplimiento — requisitos obligatorios de la pauta

| Requisito obligatorio | Estado real | Servicios OK | Servicios con hueco |
|---|---|---|---|
| Patrón CSR completo (controller→service→repository) | Parcial | 8/10 dominio | `equipment-service`, `notification-service` sin capa `service` |
| DTOs separados de entidad (creación/respuesta) | ❌ Mayoritario | `user`, `branch` | `membership`, `access`, `qr-generator`, `capacity`, `class`, `routine`, `equipment`, `notification` |
| Bean Validation (`@NotNull`, etc.) + `@Valid` en controller | ❌ No existe en ningún servicio | ninguno | los 10 |
| `@ControllerAdvice` con JSON de error uniforme | Parcial | `user`, `branch` | `membership`, `access`, `qr-generator`, `capacity`, `class`, `routine`, `equipment`, `notification` |
| Comunicación Feign real (invocada, no solo declarada) | Parcial | `access→capacity`, `access→membership` (sí se llaman, hay que confirmar en `AccessService.java`) | `user→branch` (`BranchClient` existe pero nunca se inyecta/llama — código muerto) |
| Comunicación RestClient/WebClient real | ❌ No existe | — | los 12 (cero usos de `RestTemplate`/`WebClient`/`RestClient` en todo el repo) |
| Seguridad real (roles, reglas por endpoint, 401/403) | ❌ Superficial | — | `user-service` tiene BCrypt pero `SecurityConfig` usa `.anyRequest().permitAll()` → sin autorización real. El resto de servicios no tiene dependencia `spring-security` siquiera |
| Gateway: rutas `lb://` | ✅ | 10/10 rutas definidas correctamente en `gateway-service/application.yml` | — |
| Gateway: filtro `X-Request-Id` / trazabilidad | ❌ No existe | — | gateway no tiene filtros globales configurados |
| Eureka + registro dinámico | ✅ | 12/12 | — |
| Flyway versionado | ✅ | 10/10 servicios de dominio tienen `V1__...sql` | — |
| Swagger con `@Operation`/`@ApiResponse` | ❌ No existe | — | los 12 (solo autogenerado, dependencia `springdoc` presente pero sin anotaciones) |
| `System.out.println` como logging | ✅ (no hay) | 12/12 usan SLF4J correctamente donde hay logs | — |
| Pruebas unitarias | Parcial | `user`, `branch`, `membership`, `access`, `qr-generator` (5) | `capacity`, `class`, `routine`, `equipment`, `notification` (5 sin tests) |
| Driver Postgres en `pom.xml` | ❌ No existe | — | los 12 (todos con `h2` únicamente, ninguno listo para Render con BD persistente) |
| `.env.example` / variables de entorno para credenciales | Por confirmar | — | pendiente de revisar (no se encontró en el listado raíz) |

## 3. Matriz por microservicio (resumen rápido)

| Servicio | Puerto | Service layer | DTOs | Validación | ControllerAdvice | Tests | Feign/RestClient | Seguridad |
|---|---|---|---|---|---|---|---|---|
| eureka-server | 8761 | n/a | n/a | n/a | n/a | ❌ | n/a | n/a |
| gateway-service | 8082 | n/a | n/a | n/a | n/a | ❌ | n/a | falta filtro X-Request-Id |
| user-service | 8080 | ✅ | ✅ | ❌ | ✅ | ✅ | Feign declarado, no usado | BCrypt sí, autorización no (permitAll) |
| branch-service | 8081 | ✅ | ✅ | ❌ | ✅ | ✅ | — | ❌ |
| membership-service | 8083 | ✅ | ❌ | ❌ | ❌ | ✅ | — | ❌ |
| access-service | 8084 | ✅ | ❌ | ❌ | ❌ | ✅ | Feign x2, sí declarados con `@FeignClient` (confirmar uso real) | ❌ |
| qr-generator-service | 8085 | ✅ | ❌ | ❌ | ❌ | ✅ | — | ❌ |
| capacity-service | 8086 | ✅ | ❌ | ❌ | ❌ | ❌ | — | ❌ |
| class-service | 8087 | ✅ | ❌ | ❌ | ❌ | ❌ | — | ❌ |
| routine-service | 8088 | ✅ | ❌ | ❌ | ❌ | ❌ | — | ❌ |
| equipment-service | 8089 | ❌ sin capa service | ❌ | ❌ | ❌ | ❌ | — | ❌ |
| notification-service | 8090 | ❌ sin capa service | ❌ | ❌ | ❌ | ❌ | — | ❌ |

## 4. Punto de mayor riesgo: Render para los 12 servicios

Confirmaste que es exigencia real del profesor. Ten en cuenta antes de empezar:

- Hoy ningún servicio tiene driver Postgres ni perfil `render`/`prod`. Hay que agregarlo a los 12 (mínimo: dependencia `postgresql`, perfil con `SPRING_DATASOURCE_URL` por variable de entorno, ajuste de dialecto).
- Yo no tengo acceso a tu cuenta de Render — no puedo crear los servicios ni pegar variables de entorno en su dashboard directamente desde este entorno sandbox. Dos formas de resolverlo:
  1. Te preparo todo (Dockerfiles ok, `render.yaml` Blueprint con los 12 servicios + variables, checklist paso a paso) y tú ejecutas la creación en el dashboard de Render (15-20 min si el Blueprint está bien armado).
  2. Si me das acceso a tu navegador (Claude in Chrome) y ya tienes sesión iniciada en Render, puedo operar el dashboard por ti — pero cada servicio tarda varios minutos en buildear, y con 12 servicios esto puede consumir la mayor parte de las 4 horas solo esperando builds.
- Con 4 horas totales, es matemáticamente ajustado hacer: (a) DTOs+validación+ControllerAdvice en 8 servicios, (b) service layer en 2, (c) Feign real + RestClient nuevo, (d) seguridad con roles, (e) 5 servicios de test nuevos, (f) Swagger anotado en 12, y (g) migrar 12 a Postgres + desplegar 12 en Render. Lo voy a intentar en el orden que definamos, pero si el tiempo aprieta, avisaré antes de que sea tarde para recortar, no después.

## 5. Plan de trabajo propuesto para las próximas ~4 horas

Orden pensado para: no dejar nada "declarado pero no implementado" (regla de 0 automático), y priorizar lo que más pesa en tu defensa individual (60% de la nota, con pruebas en vivo 12% y modificación en vivo 10% como los ítems más grandes).

1. **DTOs + Bean Validation + `@Valid` + `@ControllerAdvice` uniforme** en los 8 servicios que no lo tienen. Es el hueco más grande y transversal — sin esto, varios ítems de la rúbrica están en 0 directo.
2. **Capa service** en `equipment-service` y `notification-service` (la deuda que ya vio el profesor).
3. **Feign real** (arreglar `user→branch` para que se invoque de verdad) + **una comunicación RestClient/WebClient nueva** con DTO remoto, timeout y manejo de error.
4. **Seguridad real**: roles `socio`/`administrador`, reglas por endpoint, 401 vs 403 — hoy es decorativa (permitAll).
5. **Filtro `X-Request-Id`** en el Gateway.
6. **Pruebas unitarias** en los 5 servicios que no tienen (`capacity`, `class`, `routine`, `equipment`, `notification`), foco en capa service.
7. **Swagger anotado** (`@Operation`, `@ApiResponse`, ejemplos) en los 12, verificando que coincida con el comportamiento real.
8. **Postgres + Render** para los 12 servicios (el bloque más largo, se deja al final del tiempo disponible pero con margen fijo reservado, no "lo que sobre").
9. **Documentación** (README, matriz de requerimientos, plan de cierre de feedback, etc.) — se arma en paralelo con evidencia real de lo ya corregido, no antes.

## 6. Antes de tocar código, necesito que confirmes

- ¿Partimos por el punto 1 (DTOs/validación/errores) tal como está priorizado, o prefieres otro orden?
- Para Render: ¿ya tienes cuenta creada? ¿Prefieres que te arme el Blueprint (`render.yaml`) + guía paso a paso, o que intente operarlo yo vía navegador si me das acceso?
- ¿Tienes credenciales de Postgres en mente (Render Postgres free tier) o debo asumir que las crearemos durante el proceso?
