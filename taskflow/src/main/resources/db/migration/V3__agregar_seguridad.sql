-- -------------------------------------------------------
-- V3: Agrega seguridad a la tabla users
-- Nuevas columnas: password (hash BCrypt) y rol (USER/ADMIN)
-- -------------------------------------------------------

-- Agregar columna password
ALTER TABLE users ADD COLUMN IF NOT EXISTS password VARCHAR(255) NOT NULL DEFAULT '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy';
-- El DEFAULT es el hash BCrypt de "password123" — solo para datos existentes

-- Agregar columna rol
ALTER TABLE users ADD COLUMN IF NOT EXISTS rol VARCHAR(20) NOT NULL DEFAULT 'USER';

-- Actualizar el usuario existente (Ana García) con datos reales
UPDATE users SET
                 password = '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
                 rol = 'ADMIN'
WHERE email = 'ana@taskflow.com';

ALTER TABLE users ADD COLUMN IF NOT EXISTS password VARCHAR(255) NOT NULL DEFAULT '$2a$10$gQbaHGnxnHd/6lcbAfdlE.l.81vqysdVpqVpk2IqqpCji2uAPzaXu';

UPDATE users SET
                 password = '$2a$10$gQbaHGnxnHd/6lcbAfdlE.l.81vqysdVpqVpk2IqqpCji2uAPzaXu',
                 rol = 'ADMIN'
WHERE email = 'ana@taskflow.com';