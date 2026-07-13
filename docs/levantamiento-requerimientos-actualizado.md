# Levantamiento de requerimientos — actualizado al cierre

## Requerimientos funcionales

| ID | Requerimiento | Servicio(s) | Estado |
|---|---|---|---|
| RF-01 | Registrar usuarios con rol (socio/administrador) y sucursal asociada | user-service, branch-service | ✅ |
| RF-02 | Autenticar usuarios y restringir endpoints por rol | user-service | ✅ |
| RF-03 | Gestionar sucursales (alta, consulta) | branch-service | ✅ |
| RF-04 | Gestionar membresías y su estado (activa/inactiva) | membership-service | ✅ |
| RF-05 | Generar códigos QR de acceso | qr-generator-service | ✅ |
| RF-06 | Validar acceso físico contra QR + membresía activa | access-service | ✅ |
| RF-07 | Controlar aforo en tiempo real por sucursal | capacity-service | ✅ |
| RF-08 | Reservar clases validando membresía activa | class-service | ✅ |
| RF-09 | Evitar reservas duplicadas de un socio en la misma clase | class-service | ✅ |
| RF-10 | Asignar y consultar rutinas de entrenamiento | routine-service | ✅ |
| RF-11 | Gestionar inventario de equipos con estado | equipment-service | ✅ |
| RF-12 | Registrar notificaciones enviadas a socios | notification-service | ✅ |

## Requerimientos no funcionales

| ID | Requerimiento | Estado | Evidencia |
|---|---|---|---|
| RNF-01 | Arquitectura de microservicios con registro dinámico | ✅ | Eureka, 12/12 servicios registrados |
| RNF-02 | Punto de entrada único con balanceo de carga | ✅ | Gateway con rutas `lb://` |
| RNF-03 | Trazabilidad de requests entre servicios | ✅ | Filtro `X-Request-Id` en el Gateway |
| RNF-04 | Validación de datos de entrada | ✅ | Bean Validation + `@Valid` en los 10 servicios de dominio |
| RNF-05 | Manejo uniforme de errores | ✅ | `GlobalExceptionHandler` con `ErrorResponse` estándar en los 12 |
| RNF-06 | Persistencia real para datos críticos | ✅ | Postgres en `user`, `branch`, `membership`, `access` |
| RNF-07 | Autenticación y autorización | ✅ | BCrypt + roles + 401/403 en `user-service` |
| RNF-08 | Documentación de API navegable | ✅ | Swagger UI anotado en los 12 servicios |
| RNF-09 | Migraciones de base de datos versionadas | ✅ | Flyway en los servicios con BD relacional |
| RNF-10 | Cobertura de pruebas unitarias en lógica de negocio | ✅ | Tests en capa `service` de los 10 servicios de dominio |
| RNF-11 | Despliegue en entorno remoto accesible | 🔶 Preparado, pendiente de ejecución manual | `render.yaml` listo, falta que Eduardo cree la cuenta y despliegue |

## Cambios respecto al levantamiento original

Durante el diagnóstico previo a esta sesión se detectó que el levantamiento original no
capturaba explícitamente RF-09 (evitar reservas duplicadas) ni el detalle de RNF-07
(autorización por rol, no solo autenticación). Ambos se agregaron aquí porque terminaron siendo
parte de la implementación final y son defendibles como reglas de negocio explícitas.
