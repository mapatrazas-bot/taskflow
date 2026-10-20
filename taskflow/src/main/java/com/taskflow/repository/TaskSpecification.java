package com.taskflow.repository;

import com.taskflow.domain.enums.Category;
import com.taskflow.domain.enums.Priority;
import com.taskflow.domain.enums.TaskStatus;
import com.taskflow.entity.TaskEntity;
import org.springframework.data.jpa.domain.Specification;

/**
 * Especificaciones para filtros dinámicos de tareas.
 *
 * <p><b>Concepto JPA — Specification (patrón Criteria):</b>
 * permite construir queries dinámicas combinando condiciones en tiempo
 * de ejecución. Cada método retorna una {@code Specification} que puede
 * combinarse con {@code and()} y {@code or()} como un predicado booleano.</p>
 *
 * <p>Equivalente en SQL: WHERE status = ? AND categoria = ? AND prioridad = ?
 * donde cada condición es opcional según lo que llegue en los query params.</p>
 *
 * <p>Es el equivalente JPA del {@code TaskFilter.coincide()} de la Fase 1,
 * pero ejecutado directamente en la base de datos.</p>
 */
public class TaskSpecification {

    private TaskSpecification() {} // clase utilitaria, no instanciar

    /**
     * Filtra por estado si el valor no es null.
     * Si es null, retorna una spec "vacía" que no agrega condición.
     */
    public static Specification<TaskEntity> conStatus(TaskStatus status) {
        return (root, query, cb) ->
                status == null ? cb.conjunction() : cb.equal(root.get("status"), status);
    }

    /** Filtra por prioridad. */
    public static Specification<TaskEntity> conPrioridad(Priority prioridad) {
        return (root, query, cb) ->
                prioridad == null ? cb.conjunction() : cb.equal(root.get("prioridad"), prioridad);
    }

    /** Filtra por categoría. */
    public static Specification<TaskEntity> conCategoria(Category categoria) {
        return (root, query, cb) ->
                categoria == null ? cb.conjunction() : cb.equal(root.get("categoria"), categoria);
    }

    /** Búsqueda de texto en título (LIKE %texto%, insensible a mayúsculas). */
    public static Specification<TaskEntity> tituloContiene(String texto) {
        return (root, query, cb) ->
                texto == null || texto.isBlank()
                        ? cb.conjunction()
                        : cb.like(cb.lower(root.get("titulo")), "%" + texto.toLowerCase() + "%");
    }

    /** Filtra por ID de usuario. */
    public static Specification<TaskEntity> deUsuario(Long usuarioId) {
        return (root, query, cb) ->
                usuarioId == null ? cb.conjunction()
                        : cb.equal(root.get("usuario").get("id"), usuarioId);
    }

    /**
     * Combina todos los filtros con AND.
     * Los filtros null se ignoran automáticamente (cb.conjunction() = TRUE).
     *
     * Uso en el servicio:
     * taskRepository.findAll(TaskSpecification.construir(status, prioridad, cat, texto), pageable)
     */
    public static Specification<TaskEntity> construir(
            TaskStatus status, Priority prioridad,
            Category categoria, String texto) {
        return Specification
                .where(conStatus(status))
                .and(conPrioridad(prioridad))
                .and(conCategoria(categoria))
                .and(tituloContiene(texto));
    }
}