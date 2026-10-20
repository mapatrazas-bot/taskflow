-- ─────────────────────────────────────────────────────────────
-- V1: Creación de tablas iniciales
-- Flyway ejecuta este script UNA SOLA VEZ al arrancar
-- El nombre V1__ indica la versión (Flyway los ejecuta en orden)
-- ─────────────────────────────────────────────────────────────

-- Tabla de usuarios
CREATE TABLE users (
                       id              BIGINT          NOT NULL,
                       nombre          VARCHAR(100)    NOT NULL,
                       email           VARCHAR(150)    NOT NULL UNIQUE,
                       fecha_creacion  TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       fecha_actualizacion TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       CONSTRAINT pk_users PRIMARY KEY (id)
);

-- Tabla de tareas
CREATE TABLE tasks (
                       id              BIGINT          NOT NULL,
                       titulo          VARCHAR(100)    NOT NULL,
                       descripcion     VARCHAR(500),
                       status          VARCHAR(20)     NOT NULL DEFAULT 'PENDIENTE',
                       prioridad       VARCHAR(20)     NOT NULL,
                       categoria       VARCHAR(20)     NOT NULL,
                       fecha_limite    DATE,
                       usuario_id      BIGINT          NOT NULL,
                       fecha_creacion  TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       fecha_actualizacion TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       CONSTRAINT pk_tasks PRIMARY KEY (id),
                       CONSTRAINT fk_tasks_usuario FOREIGN KEY (usuario_id) REFERENCES users(id)
);

-- Índices para mejorar el rendimiento de las consultas más comunes
CREATE INDEX idx_tasks_usuario    ON tasks(usuario_id);
CREATE INDEX idx_tasks_status     ON tasks(status);
CREATE INDEX idx_tasks_prioridad  ON tasks(prioridad);
CREATE INDEX idx_tasks_categoria  ON tasks(categoria);

-- Secuencia para IDs autoincrementales (compatible con H2 y PostgreSQL)
CREATE SEQUENCE users_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE tasks_seq START WITH 1 INCREMENT BY 1;