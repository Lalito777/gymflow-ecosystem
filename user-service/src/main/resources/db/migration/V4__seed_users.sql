-- Usuarios de prueba para la defensa. Contrasenas reales (BCrypt, no texto plano):
--   admin@gymflow.cl  / admin123   (rol ADMIN)
--   socio@gymflow.cl   / socio123  (rol SOCIO)
-- branch_id = NULL porque estos dos se insertan directo por SQL (sin pasar por el Feign
-- de validacion de sucursal que corre solo en el flujo normal de POST /api/users).
INSERT INTO users (name, email, subscription_plan, password, role, branch_id)
VALUES ('Joaquin Sandoval', 'admin@gymflow.cl', 'PREMIUM', '$2b$10$nrjdIriGCP5Fw1cDK9bVbezxj07Zqpz3S3oJC6n4zwn5L0EyKZS1C', 'ADMIN', NULL);

INSERT INTO users (name, email, subscription_plan, password, role, branch_id)
VALUES ('Eduardo Sepulveda', 'socio@gymflow.cl', 'BASICO', '$2b$10$cNhY.hxtfV2R9spqW.rqy.sq3N/0M49jnB9rD1j73/6CaLL9kCIBu', 'SOCIO', NULL);
