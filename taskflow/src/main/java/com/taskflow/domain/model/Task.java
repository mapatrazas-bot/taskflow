package com.taskflow.domain.model;

import com.taskflow.domain.enums.Category;
import com.taskflow.domain.enums.Priority;
import com.taskflow.domain.enums.TaskStatus;
import com.taskflow.domain.exception.TaskStateException;
import com.taskflow.domain.exception.TaskValidationException;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

/**
 * Entidad principal del dominio: representa una tarea del sistema.
 *
 * <p><b>Concepto Java — Herencia:</b> extiende {@code BaseEntity},
 * heredando ID, fechas de auditoría y la lógica de {@code esReciente()}.</p>
 *
 * <p><b>Concepto Java — Encapsulamiento:</b> todos los campos son
 * {@code private}. Los setters validan los datos antes de asignarlos
 * y llaman a {@code marcarActualizado()} para mantener la auditoría.</p>
 *
 * <p><b>Concepto Java — LocalDate:</b> para la fecha límite usamos
 * {@code LocalDate} (solo fecha, sin hora), ya que la hora exacta
 * no es relevante para el vencimiento de una tarea.</p>
 *
 * <p><b>Concepto Java — Optional:</b> {@code getFechaLimite()} retorna
 * {@code Optional<LocalDate>} porque la fecha límite es opcional.</p>
 */
public class Task extends BaseEntity {

    private String titulo;
    private String descripcion;
    private TaskStatus status;
    private Priority prioridad;
    private Category categoria;
    private LocalDate fechaLimite;  // puede ser null (tarea sin vencimiento)
    private Long usuarioId;

    /**
     * Constructor con todos los campos obligatorios.
     * La fecha límite es opcional y se asigna después con {@code setFechaLimite()}.
     *
     * @param titulo    título de la tarea (no puede estar vacío)
     * @param prioridad nivel de prioridad
     * @param categoria categoría temática
     * @param usuarioId ID del usuario propietario
     * @throws TaskValidationException si el título es inválido
     */
    public Task(String titulo, Priority prioridad, Category categoria, Long usuarioId) {
        super(); // asigna id y fechas desde BaseEntity
        validarTitulo(titulo);
        this.titulo = titulo.trim();
        this.prioridad = prioridad;
        this.categoria = categoria;
        this.usuarioId = usuarioId;
        this.status = TaskStatus.PENDIENTE; // toda tarea comienza PENDIENTE
    }

    // ── Getters ──────────────────────────────────────────────────────────────

    public String getTitulo() { return titulo; }
    public String getDescripcion() { return descripcion; }
    public TaskStatus getStatus() { return status; }
    public Priority getPrioridad() { return prioridad; }
    public Category getCategoria() { return categoria; }
    public Long getUsuarioId() { return usuarioId; }

    /**
     * La fecha límite es un campo opcional.
     *
     * <p><b>Concepto Java — Optional:</b> retornamos {@code Optional<LocalDate>}
     * en lugar de {@code LocalDate} que podría ser {@code null}.
     * El llamador se ve obligado a decidir qué hacer si no hay fecha.</p>
     *
     * @return {@code Optional} con la fecha límite, o vacío si no tiene
     */
    public Optional<LocalDate> getFechaLimite() {
        return Optional.ofNullable(fechaLimite);
    }

    // ── Setters con validación ────────────────────────────────────────────────

    /**
     * Actualiza el título de la tarea.
     *
     * @param titulo nuevo título (no puede ser nulo ni vacío)
     * @throws TaskValidationException si el título es inválido
     */
    public void setTitulo(String titulo) {
        validarTitulo(titulo);
        this.titulo = titulo.trim();
        marcarActualizado();
    }

    /** Actualiza la descripción (puede ser nula o vacía). */
    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
        marcarActualizado();
    }

    /**
     * Establece o elimina la fecha límite.
     *
     * @param fechaLimite fecha límite (puede ser {@code null} para eliminarla)
     * @throws TaskValidationException si la fecha es en el pasado
     */
    public void setFechaLimite(LocalDate fechaLimite) {
        if (fechaLimite != null && fechaLimite.isBefore(LocalDate.now())) {
            throw new TaskValidationException("fechaLimite",
                    "La fecha límite no puede ser una fecha pasada.");
        }
        this.fechaLimite = fechaLimite;
        marcarActualizado();
    }

    /** Actualiza la prioridad. */
    public void setPrioridad(Priority prioridad) {
        this.prioridad = prioridad;
        marcarActualizado();
    }

    // ── Lógica de negocio ────────────────────────────────────────────────────

    /**
     * Verifica si la transición al nuevo estado es válida según las reglas:
     * <ul>
     *   <li>No se puede cambiar desde un estado final (COMPLETADA, CANCELADA).</li>
     *   <li>No se puede retroceder de EN_PROGRESO a PENDIENTE.</li>
     * </ul>
     *
     * @param nuevoStatus estado al que se quiere transicionar
     * @return {@code true} si la transición está permitida
     */
    public boolean puedeTransicionarA(TaskStatus nuevoStatus) {
        // Regla 1: estados finales no admiten ningún cambio
        if (!this.status.isActivo()) {
            return false;
        }
        // Regla 2: no se puede retroceder de EN_PROGRESO a PENDIENTE
        if (this.status == TaskStatus.EN_PROGRESO && nuevoStatus == TaskStatus.PENDIENTE) {
            return false;
        }
        return true;
    }

    /**
     * Cambia el estado de la tarea, aplicando las reglas de transición.
     *
     * <p><b>Concepto Java — Excepciones personalizadas:</b> lanzamos
     * {@code TaskStateException} (con contexto del error) en lugar de
     * una excepción genérica de Java.</p>
     *
     * @param nuevoStatus estado destino
     * @throws TaskStateException si la transición no está permitida
     */
    public void cambiarStatus(TaskStatus nuevoStatus) {
        if (!puedeTransicionarA(nuevoStatus)) {
            throw new TaskStateException(this.status, nuevoStatus);
        }
        this.status = nuevoStatus;
        marcarActualizado();
    }

    /**
     * Indica si la tarea está vencida:
     * tiene fecha límite, ya pasó, y no está en un estado final.
     *
     * <p><b>Concepto Java — LocalDate.now():</b> compara la fecha límite
     * con el día de hoy.</p>
     *
     * @return {@code true} si la tarea está vencida y activa
     */
    public boolean estaVencida() {
        return fechaLimite != null
                && fechaLimite.isBefore(LocalDate.now())
                && status.isActivo();
    }

    /**
     * Calcula los días entre hoy y la fecha límite.
     * Valor negativo significa que ya venció.
     *
     * <p><b>Concepto Java — ChronoUnit:</b> {@code ChronoUnit.DAYS.between()}
     * calcula la diferencia en días entre dos fechas.</p>
     *
     * @return días restantes (negativo si vencida), o 0 si no tiene fecha límite
     */
    public long diasRestantes() {
        if (fechaLimite == null) {
            return 0L;
        }
        return ChronoUnit.DAYS.between(LocalDate.now(), fechaLimite);
    }

    // ── BaseEntity abstracto ─────────────────────────────────────────────────

    /**
     * <b>Concepto Java — Polimorfismo:</b> implementación concreta del
     * método abstracto de {@code BaseEntity}. Cada subclase da su propio
     * formato de descripción.
     */
    @Override
    public String getDescripcionCorta() {
        return String.format("Task[%d] '%s' | %s | %s",
                getId(), titulo, status.getEtiqueta(), prioridad.getEtiqueta());
    }

    @Override
    public String toString() {
        return String.format(
                "Task{id=%d, titulo='%s', status=%s, prioridad=%s, categoria=%s, vencida=%b}",
                getId(), titulo, status.getEtiqueta(),
                prioridad.getEtiqueta(), categoria.getDescripcion().split(" ")[0], estaVencida()
        );
    }

    // ── Privados ─────────────────────────────────────────────────────────────

    /** Valida que el título no sea nulo ni esté en blanco. */
    private void validarTitulo(String titulo) {
        if (titulo == null || titulo.isBlank()) {
            throw new TaskValidationException("titulo",
                    "El título es obligatorio y no puede estar en blanco.");
        }
        if (titulo.trim().length() < 3) {
            throw new TaskValidationException("titulo",
                    "El título debe tener al menos 3 caracteres.");
        }
    }
}