-- NOTA: este archivo NO se ejecuta con la configuracion actual (spring.jpa.hibernate.ddl-auto=validate).
-- import.sql solo lo procesa Hibernate cuando ddl-auto es create/create-drop/update.
-- El seed real de usuarios vive en la migracion Flyway V4__seed_users.sql.
INSERT INTO users (name, email, subscription_plan, password, role) VALUES ('Joaquin Sandoval', 'joaquin@gymflow.cl', 'PREMIUM', 'admin-password', 'ADMIN');