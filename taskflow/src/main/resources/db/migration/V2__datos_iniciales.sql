-- ─────────────────────────────────────────────────────────────
-- V2: Datos iniciales de prueba
-- Solo para desarrollo — en producción no se cargan datos
-- ─────────────────────────────────────────────────────────────

-- Usuario de prueba
INSERT INTO users (id, nombre, email, fecha_creacion, fecha_actualizacion)
VALUES (1, 'Ana García', 'ana@taskflow.com', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Tareas de prueba
INSERT INTO tasks (id, titulo, descripcion, status, prioridad, categoria, usuario_id, fecha_creacion, fecha_actualizacion)
VALUES
    (1, 'Preparar presentación Q3', 'Slides para la reunión del viernes',
     'PENDIENTE', 'ALTA', 'TRABAJO', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    (2, 'Leer Clean Code', 'Capítulos 1 al 5',
     'EN_PROGRESO', 'MEDIA', 'ESTUDIO', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    (3, 'Llamar al dentista', NULL,
     'PENDIENTE', 'BAJA', 'PERSONAL', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    (4, 'Entregar informe urgente', 'Informe mensual de métricas',
     'PENDIENTE', 'URGENTE', 'TRABAJO', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    (5, 'Revisar PRs pendientes', 'Repositorio taskflow en GitHub',
     'PENDIENTE', 'ALTA', 'TRABAJO', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Avanzar las secuencias más allá de los IDs insertados manualmente.
-- Sin esto, Hibernate genera IDs que colisionan con los datos de Flyway.
-- users_seq: último ID insertado = 1  → próximo valor = 2
-- tasks_seq: último ID insertado = 5  → próximo valor = 6
ALTER SEQUENCE users_seq RESTART WITH 2;
ALTER SEQUENCE tasks_seq RESTART WITH 6;