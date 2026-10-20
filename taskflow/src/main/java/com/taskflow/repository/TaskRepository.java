package com.taskflow.repository;

import com.taskflow.domain.enums.Category;
import com.taskflow.domain.enums.Priority;
import com.taskflow.domain.enums.TaskStatus;
import com.taskflow.entity.TaskEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Repositorio de Spring Data JPA para tareas.
 *
 * <p><b>Concepto Spring Data — JpaRepository:</b> interfaz genérica que
 * provee automáticamente: save(), findById(), findAll(), deleteById(),
 * count(), existsById() y más — sin escribir ninguna implementación.</p>
 *
 * <p><b>Concepto Spring Data — Métodos derivados:</b> Spring Data lee el
 * nombre del método y genera la query SQL automáticamente:
 * {@code findByStatus} → {@code SELECT * FROM tasks WHERE status = ?}</p>
 *
 * <p><b>Concepto Spring Data — JpaSpecificationExecutor:</b> permite
 * filtros dinámicos con el patrón Specification (como el TaskFilter de Fase 1
 * pero ejecutado en la BD).</p>
 */
@Repository
public interface TaskRepository
        extends JpaRepository<TaskEntity, Long>,
        JpaSpecificationExecutor<TaskEntity> {

    // ── Métodos derivados — Spring genera el SQL por el nombre del método ─────

    /**
     * findByStatus → SELECT * FROM tasks WHERE status = ?
     * Con Pageable → agrega LIMIT/OFFSET y ORDER BY automáticamente.
     */
    Page<TaskEntity> findByStatus(TaskStatus status, Pageable pageable);

    /** findByCategoria → SELECT * FROM tasks WHERE categoria = ? */
    Page<TaskEntity> findByCategoria(Category categoria, Pageable pageable);

    /** findByPrioridad → SELECT * FROM tasks WHERE prioridad = ? */
    Page<TaskEntity> findByPrioridad(Priority prioridad, Pageable pageable);

    /** findByUsuarioId → SELECT * FROM tasks WHERE usuario_id = ? */
    Page<TaskEntity> findByUsuarioId(Long usuarioId, Pageable pageable);

    /** Combinado: status + categoria */
    Page<TaskEntity> findByStatusAndCategoria(
            TaskStatus status, Category categoria, Pageable pageable);

    /** Búsqueda por texto en título (LIKE %busqueda%) */
    Page<TaskEntity> findByTituloContainingIgnoreCase(String titulo, Pageable pageable);

    // ── @Query con JPQL — cuando los métodos derivados no alcanzan ────────────

    /**
     * Concepto JPA — @Query con JPQL:
     * JPQL usa nombres de clases Java (TaskEntity, t.fechaLimite)
     * no nombres de tablas SQL (tasks, fecha_limite).
     * El compilador de Hibernate valida la query al arrancar.
     */
    @Query("SELECT t FROM TaskEntity t WHERE t.fechaLimite < :hoy AND t.status IN :estados")
    List<TaskEntity> findVencidas(
            @Param("hoy") LocalDate hoy,
            @Param("estados") List<TaskStatus> estados);

    /**
     * Conteo de tareas por estado para estadísticas.
     * Retorna una lista de arrays Object[]: [status, count]
     */
    @Query("SELECT t.status, COUNT(t) FROM TaskEntity t GROUP BY t.status")
    List<Object[]> contarPorEstado();

    /**
     * Proyección — solo retorna los campos definidos en TareaResumen.
     * Spring genera: SELECT t.id, t.titulo, t.status, t.prioridad, u.nombre
     * en lugar de SELECT * (más eficiente).
     */
    @Query("SELECT t.id AS id, t.titulo AS titulo, t.status AS status, " +
            "t.prioridad AS prioridad, t.usuario.nombre AS usuarioNombre " +
            "FROM TaskEntity t WHERE t.usuario.id = :usuarioId")
    List<TareaResumen> findResumenByUsuarioId(@Param("usuarioId") Long usuarioId);

    /** Cuenta tareas de un usuario por estado. */
    long countByUsuarioIdAndStatus(Long usuarioId, TaskStatus status);
}