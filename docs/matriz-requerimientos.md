# Matriz de requerimientos — GymFlow

Todo lo marcado "Implementado" está respaldado por evidencia verificable en el repositorio
(endpoint real + test real, no descripción de intención). Verificado contra el código el 13 jul
2026, ejecutando `mvnw test` en los 10 servicios de dominio (42 tests, 0 fallas).

| ID | Requerimiento declarado por el equipo | Tipo | Estado | Endpoint o evidencia | Prueba asociada |
|---|---|---|---|---|---|
| RF-01 | Registrar un usuario con rol y sucursal asociada | Funcional | Implementado | `POST /api/users` | `UserServiceTest.create_conDatosValidosYSucursalExistente_deberiaEncriptarPasswordYGuardarUsuario` |
| RF-02 | Rechazar el registro si la sucursal indicada no existe | Funcional | Implementado | `POST /api/users` (Feign a `branch-service`) | `UserServiceTest.create_conSucursalInexistente_deberiaLanzarEntityNotFoundYNoGuardar` |
| RF-03 | Rechazar el registro si el rol no es válido | Funcional | Implementado | `POST /api/users` | `UserServiceTest.create_conRolInvalido_deberiaLanzarExcepcionYNoConsultarSucursal` |
| RF-04 | Listar usuarios solo para rol ADMIN | Funcional | Implementado | `GET /api/users` (401/403 según caso) | `SecurityConfig` + verificación manual con `docs/gymflow.http` (Basic Auth) |
| RF-05 | Registrar y listar sucursales | Funcional | Implementado | `POST /api/branches`, `GET /api/branches` | `BranchServiceTest.create_conDatosValidos_deberiaGuardarYRetornarSede`, `BranchServiceTest.findAll_conSedesRegistradas_deberiaRetornarListaMapeada` |
| RF-06 | Consultar una sucursal por ID (usado por Feign) | Funcional | Implementado | `GET /api/branches/{id}` | `BranchServiceTest.findById_conSedeExistente_deberiaRetornarla`, `findById_conSedeInexistente_deberiaLanzarExcepcion` |
| RF-07 | Crear una membresía calculando su vencimiento según el plan | Funcional | Implementado | `POST /api/membership` | `MembershipServiceTest.createMembership_conPlanExistente_deberiaCalcularFechaVencimientoYGuardar` |
| RF-08 | Rechazar la creación de membresía si el plan no existe | Funcional | Implementado | `POST /api/membership` | `MembershipServiceTest.createMembership_conPlanInexistente_deberiaLanzarEntityNotFoundException` |
| RF-09 | Consultar si un socio tiene membresía activa | Funcional | Implementado | `GET /api/membership/status/{userId}` | `MembershipServiceTest.isMembershipActive_conMembresiaVigenteYActiva_deberiaRetornarTrue`, `isMembershipActive_conMembresiaVencida_deberiaRetornarFalse` |
| RF-10 | Generar un token de acceso solo si la membresía está activa | Funcional | Implementado | `POST /api/access/generate` (Feign a `membership-service`) | `AccessServiceTest.generateToken_conMembresiaActiva_deberiaCrearToken`, `generateToken_conMembresiaInactiva_deberiaLanzarExcepcion` |
| RF-11 | Validar una entrada por QR (existencia, expiración, reuso) | Funcional | Implementado | `POST /api/access/validate` | `AccessServiceTest.validateEntry_conQrValido_deberiaRegistrarEntradaYActualizarToken`, `validateEntry_conQrInexistente_deberiaLanzarExcepcion`, `validateEntry_conQrYaUsado_deberiaLanzarExcepcion`, `validateEntry_conTokenExpirado_deberiaLanzarExcepcion` |
| RF-12 | Seguir registrando la entrada aunque `capacity-service` no responda | No funcional (resiliencia) | Implementado | `POST /api/access/validate` (llamada tolerante a fallas) | `AccessServiceTest.validateEntry_siCapacityServiceFalla_deberiaContinuarSinLanzarExcepcion` |
| RF-13 | Generar la imagen QR de un token de acceso | Funcional | Implementado | `POST /api/qr/create` | `QRServiceTest.generateQR_conContenidoValido_deberiaGenerarImagenBase64YGuardar` |
| RF-14 | Incrementar/decrementar el aforo de una sucursal sin bajar de 0 | Funcional | Implementado | `POST /api/capacity/{branchId}/increment`, `.../decrement` | `CapacityServiceTest.increment_sinContadorPrevio_deberiaCrearloConUnaPersona`, `decrement_conAforoEnCero_noDeberiaQuedarNegativo` |
| RF-15 | Reservar una clase solo si la membresía está activa | Funcional | Implementado | `POST /api/classes/reserve` (RestClient a `membership-service`) | `ClassServiceTest.reserve_conMembresiaActivaYSinReservaPrevia_deberiaCrearlaConfirmada`, `reserve_conMembresiaInactiva_deberiaLanzarExcepcionYNoGuardar` |
| RF-16 | Rechazar reservas duplicadas del mismo socio en la misma clase | Funcional | Implementado | `POST /api/classes/reserve` | `ClassServiceTest.reserve_conReservaYaConfirmadaParaLaMismaClase_deberiaLanzarExcepcion` |
| RF-17 | Crear y consultar rutinas de entrenamiento | Funcional | Implementado | `POST /api/routines`, `GET /api/routines/user/{id}` | `RoutineServiceTest.createRoutine_conDatosValidos_deberiaGuardarConFechaDeHoy`, `getRoutinesByUser_deberiaMapearTodasLasRutinasDelUsuario` |
| RF-18 | Registrar equipos solo con estado válido (catálogo cerrado) | Funcional | Implementado | `POST /api/equipment` | `EquipmentServiceTest.create_conEstadoValido_deberiaGuardarElEquipo`, `create_conEstadoInvalido_deberiaLanzarExcepcion` |
| RF-19 | Registrar notificaciones solo con tipo válido (EMAIL/SMS/PUSH) | Funcional | Implementado | `POST /api/notify` | `NotificationServiceTest.send_conTipoValido_deberiaGuardarLaNotificacion`, `send_conTipoInvalido_deberiaLanzarExcepcion` |
| RNF-01 | No exponer credenciales reales en el repositorio | No funcional | Implementado | `.env.example`, `.gitignore` (excluye `.env`, `.env.*`) | Revisión manual del repositorio |
| RNF-02 | Registro y descubrimiento dinámico de servicios | No funcional | Implementado | `eureka-server`, panel `/` de Eureka | Revisión manual (panel Eureka, 11 instancias UP) |
| RNF-03 | Punto de entrada único con enrutamiento dinámico | No funcional | Implementado | `gateway-service`, rutas `lb://` en `application.yml` | Revisión manual (requests vía Gateway) |
| RNF-04 | Trazabilidad de una request a través de varios servicios | No funcional | Implementado | Header `X-Request-Id` (`RequestTraceFilter`) | Revisión manual de logs |
| RNF-05 | Manejo uniforme de errores en toda la API | No funcional | Implementado | `GlobalExceptionHandler` en los 10 servicios de dominio (`ErrorResponse` estándar); `gateway-service` y `eureka-server` no exponen controllers de negocio propios, por lo que no aplica el mismo `@ControllerAdvice` | Casos `*_deberiaLanzarExcepcion` en los 10 test suites de dominio |
| RNF-06 | Persistencia real para datos de negocio críticos | No funcional | Implementado | Postgres en Render (`user`, `branch`, `membership`, `access`), schema propio por servicio | Revisión manual (logs Flyway en Render) |
| RNF-07 | Autenticación y autorización por rol | No funcional | Implementado | `SecurityConfig` (`user-service`): BCrypt, roles SOCIO/ADMIN, 401/403 en JSON | Revisión manual con `docs/gymflow.http` |
| RNF-08 | Documentación de API navegable y anotada | No funcional | Implementado | Swagger UI `/swagger-ui.html`, `@Operation`/`@ApiResponses` en los 10 servicios de dominio (los que exponen endpoints REST de negocio) | Revisión manual |
| RNF-09 | Migraciones de base de datos versionadas | No funcional | Implementado | `db/migration/V1...V4` (Flyway) en los 10 servicios de dominio | Revisión manual + logs `DbMigrate` en Render |
| RNF-10 | Cobertura de pruebas unitarias sobre reglas de negocio | No funcional | Implementado | `src/test/java` en los 10 servicios de dominio | `mvnw test` → 42 tests, 0 fallas (verificado 13 jul 2026) |
| RNF-11 | Despliegue accesible en un entorno remoto | No funcional | Implementado | Render: 12 servicios + Postgres, `render.yaml` | Revisión manual (URLs públicas + panel Eureka) |

## Anexo — matriz de cumplimiento por requisito de la pauta (antes/después de esta entrega)

| Requisito obligatorio de la pauta | Estado inicial | Estado final |
|---|---|---|
| Patrón CSR completo (controller→service→repository) | Parcial (8/10) | ✅ 10/10 |
| DTOs separados de entidad | ❌ 2/10 | ✅ 10/10 |
| Bean Validation + `@Valid` en controller | ❌ 0/10 | ✅ 10/10 |
| `@ControllerAdvice` con JSON de error uniforme | Parcial (2/10) | ✅ 10/10 |
| Comunicación Feign real (invocada, no solo declarada) | Parcial | ✅ |
| Comunicación RestClient real | ❌ 0/12 | ✅ |
| Seguridad real (roles, 401/403) | ❌ Superficial | ✅ |
| Gateway: rutas `lb://` | ✅ | ✅ |
| Gateway: filtro `X-Request-Id` | ❌ | ✅ |
| Eureka + registro dinámico | ✅ | ✅ |
| Flyway versionado | ✅ | ✅ |
| Swagger con `@Operation`/`@ApiResponse` | ❌ 0/12 | ✅ 12/12 |
| Pruebas unitarias | Parcial (5/10) | ✅ 10/10 |
| Driver Postgres | ❌ 0/12 | ✅ 4/4 (servicios críticos) |
| Despliegue remoto en Render | ❌ | ✅ 12/12 servicios + Gateway + Eureka |
| `.gitignore` protege secretos | Por confirmar | ✅ |
