-- Sin esto, la tabla "plans" queda vacia en cada entorno nuevo (Render, H2 local limpio) y
-- POST /api/membership siempre falla con 404 "el plan no existe", porque no hay ningun endpoint
-- para crear planes via API (los planes son catalogo fijo, no algo que el usuario cree).
INSERT INTO plans (nombre, precio, duracion_dias, descripcion) VALUES
    ('BASICO', 19990.00, 30, 'Acceso a sala de musculacion, 1 sucursal'),
    ('PREMIUM', 29990.00, 30, 'Acceso a todas las sucursales + clases grupales'),
    ('VIP', 49990.00, 30, 'Acceso ilimitado + rutina personalizada + invitados');
