package com.taskflow.repository;

import com.taskflow.domain.enums.Priority;
import com.taskflow.domain.enums.TaskStatus;

/**
 * Proyección de Spring Data JPA.
 *
 * <p><b>Concepto JPA — Proyecciones:</b> en lugar de cargar toda la entidad
 * {@code TaskEntity} con todos sus campos, una proyección define exactamente
 * qué columnas se recuperan de la BD. Mejora el rendimiento porque
 * se genera un {@code SELECT id, titulo, status, prioridad} en lugar de
 * {@code SELECT *}.</p>
 *
 * <p>Spring Data detecta esta interfaz y genera el proxy automáticamente.
 * Los nombres de los métodos deben coincidir con los getters de la entidad.</p>
 */
public interface TareaResumen {

    Long getId();
    String getTitulo();
    TaskStatus getStatus();
    Priority getPrioridad();

    /**
     * Método derivado del campo relacionado.
     * Spring Data genera: LEFT JOIN users ON tasks.usuario_id = users.id
     * y retorna users.nombre como getNombreUsuario().
     */
    String getUsuarioNombre(); // accede a usuario.nombre de la relación
}