# Documentación funcional — GymFlow

## Descripción general

GymFlow es un sistema de gestión para una cadena de gimnasios con múltiples sucursales. Cubre
el ciclo completo de un socio: registro, membresía, acceso físico al gimnasio, reserva de
clases, seguimiento de rutinas y notificaciones.

## Actores

- **Socio**: usuario final del gimnasio. Puede consultar su membresía, reservar clases, ver
  rutinas asignadas.
- **Administrador**: gestiona usuarios, sucursales, equipos y el catálogo general.
- **Sistema de acceso** (torniquete/QR): valida entradas físicas contra membresía activa.

## Flujos principales

### 1. Registro de un socio nuevo
1. Un administrador crea el usuario (`POST /api/users`) indicando nombre, email, plan de
   suscripción, contraseña, rol y la sucursal a la que pertenece.
2. `user-service` valida los datos (Bean Validation) y confirma con `branch-service` que la
   sucursal indicada existe antes de guardar (Feign). Si no existe, responde 404.
3. La contraseña se guarda hasheada con BCrypt, nunca en texto plano.

### 2. Alta y consulta de membresía
1. Se crea una membresía asociada a un `userId` (`POST /api/membership`).
2. Otros servicios consultan si un usuario tiene membresía activa vía
   `GET /api/membership/status/{userId}`, que devuelve un DTO simple (`activa: true/false`).

### 3. Control de acceso físico (flujo de 3 pasos, 3 servicios distintos)
1. **Generar token** (`POST /api/access/generate` en `access-service`, con `userId` y
   `branchId`): antes de crear el token, `access-service` consulta a `membership-service`
   (Feign) si el usuario tiene membresía activa. Si no la tiene, se rechaza aquí mismo (409) y
   nunca se llega a emitir un QR.
2. **Generar la imagen QR** (`POST /api/qr/create` en `qr-generator-service`, con el
   `accessTokenId` y el código del paso anterior): genera la imagen (Base64) que se le muestra
   al socio.
3. **Validar la entrada** (`POST /api/access/validate` en `access-service`, con el `qrCode` y
   `branchId`): revisa que el token exista, no haya expirado y no haya sido usado antes; lo
   marca como `USADO` (evita reuso) y notifica a `capacity-service` para incrementar el aforo de
   la sucursal (de forma tolerante a fallas: si `capacity-service` no responde, el ingreso se
   registra igual, solo se pierde el incremento de aforo).

La validación de membresía activa ocurre al **generar** el token, no al validarlo — una vez que
el token existe, ya se confirmó que la membresía estaba activa en ese momento.

### 4. Reserva de clases
1. Un socio reserva un cupo en una clase (`POST /api/classes/reserve`).
2. `class-service` verifica con `membership-service` (vía `RestClient`, no Feign) que la
   membresía esté activa antes de confirmar la reserva.
3. Se evita doble reserva del mismo usuario en la misma clase (regla de negocio en la capa
   `service`).

### 5. Aforo en tiempo real
- `capacity-service` mantiene un contador de personas por sucursal, incrementado al ingresar y
  decrementado al salir (nunca baja de 0).

### 6. Rutinas, equipos y notificaciones
- `routine-service`: asignación de rutinas de entrenamiento a socios.
- `equipment-service`: inventario de equipos con estado (`DISPONIBLE`, `EN_MANTENCION`,
  `FUERA_DE_SERVICIO`).
- `notification-service`: registro de notificaciones enviadas a socios (`EMAIL`, `SMS`,
  `PUSH`).

## Reglas de negocio destacadas (las que más se prestan a preguntas de defensa)

- Un usuario no puede crearse si la sucursal indicada no existe.
- Un acceso no se autoriza si la membresía no está activa, aunque el QR sea válido.
- Una reserva de clase no se confirma si la membresía no está activa.
- Un socio no puede reservar dos veces la misma clase.
- El aforo nunca puede ser negativo.
- Un equipo o notificación con un estado/tipo fuera del catálogo permitido se rechaza (400).
- Solo un ADMIN puede listar todos los usuarios (`GET /api/users`); cualquier otro rol recibe
  403.
