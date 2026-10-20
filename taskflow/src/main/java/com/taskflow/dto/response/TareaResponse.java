package com.taskflow.dto.response;

import com.taskflow.domain.enums.Category;
import com.taskflow.domain.enums.Priority;
import com.taskflow.domain.enums.TaskStatus;
import com.taskflow.domain.model.Task;
import com.taskflow.entity.TaskEntity;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO de respuesta para representar una tarea en la API.
 *
 * <p><b>Concepto REST — DTO de Response separado del Request:</b>
 * la respuesta puede incluir campos calculados que no vienen del cliente
 * (como {@code estaVencida}, {@code diasRestantes}, {@code fechaCreacion}).
 * Nunca exponemos la entidad de dominio directamente — controlamos
 * exactamente qué campos ve el cliente.</p>
 */
public class TareaResponse {

    private Long id;
    private String titulo;
    private String descripcion;
    private TaskStatus estado;
    private String estadoEtiqueta;
    private Priority prioridad;
    private String prioridadEtiqueta;
    private Category categoria;
    private LocalDate fechaLimite;
    private boolean estaVencida;
    private long diasRestantes;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;

    public TareaResponse() {}

    // ── Factory method Fase 1-3 — desde entidad de dominio Task ──────────────

    /**
     * Convierte la entidad de dominio Task (en memoria) al DTO de respuesta.
     * Usado por TaskService (almacén en memoria) de las Fases 1-3.
     */
    public static TareaResponse fromTask(Task task) {
        TareaResponse dto = new TareaResponse();
        dto.id = task.getId();
        dto.titulo = task.getTitulo();
        dto.descripcion = task.getDescripcion();
        dto.estado = task.getStatus();
        dto.estadoEtiqueta = task.getStatus().getEtiqueta();
        dto.prioridad = task.getPrioridad();
        dto.prioridadEtiqueta = task.getPrioridad().getEtiqueta();
        dto.categoria = task.getCategoria();
        dto.fechaLimite = task.getFechaLimite().orElse(null);
        dto.estaVencida = task.estaVencida();
        dto.diasRestantes = task.diasRestantes();
        dto.fechaCreacion = task.getFechaCreacion();
        dto.fechaActualizacion = task.getFechaActualizacion();
        return dto;
    }

    // ── Factory method Fase 4 — desde entidad JPA TaskEntity ─────────────────

    /**
     * Convierte la entidad JPA TaskEntity (base de datos) al DTO de respuesta.
     * Usado por TaskJpaService a partir de la Fase 4.
     *
     * <p><b>¿Por qué dos métodos fromTask?</b>
     * Fase 1-3 usaba la clase de dominio {@code Task} (en memoria).
     * Fase 4 usa {@code TaskEntity} (JPA/Hibernate). Son clases diferentes
     * — la entidad JPA tiene anotaciones @Entity, @Column, etc.
     * mientras que la clase de dominio es Java puro.</p>
     */
    public static TareaResponse fromTaskEntity(TaskEntity entity) {
        TareaResponse dto = new TareaResponse();
        dto.id = entity.getId();
        dto.titulo = entity.getTitulo();
        dto.descripcion = entity.getDescripcion();
        dto.estado = entity.getStatus();
        dto.estadoEtiqueta = entity.getStatus().getEtiqueta();
        dto.prioridad = entity.getPrioridad();
        dto.prioridadEtiqueta = entity.getPrioridad().getEtiqueta();
        dto.categoria = entity.getCategoria();
        dto.fechaLimite = entity.getFechaLimite();
        dto.estaVencida = entity.estaVencida();
        dto.diasRestantes = entity.diasRestantes();
        dto.fechaCreacion = entity.getFechaCreacion();
        dto.fechaActualizacion = entity.getFechaActualizacion();
        return dto;
    }

    // ── Getters ──────────────────────────────────────────────────────────────

    public Long getId() { return id; }
    public String getTitulo() { return titulo; }
    public String getDescripcion() { return descripcion; }
    public TaskStatus getEstado() { return estado; }
    public String getEstadoEtiqueta() { return estadoEtiqueta; }
    public Priority getPrioridad() { return prioridad; }
    public String getPrioridadEtiqueta() { return prioridadEtiqueta; }
    public Category getCategoria() { return categoria; }
    public LocalDate getFechaLimite() { return fechaLimite; }
    public boolean isEstaVencida() { return estaVencida; }
    public long getDiasRestantes() { return diasRestantes; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public LocalDateTime getFechaActualizacion() { return fechaActualizacion; }

    // ── Setters — necesarios para fromTaskEntity() ────────────────────────────

    public void setId(Long id) { this.id = id; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public void setEstado(TaskStatus estado) { this.estado = estado; }
    public void setEstadoEtiqueta(String estadoEtiqueta) { this.estadoEtiqueta = estadoEtiqueta; }
    public void setPrioridad(Priority prioridad) { this.prioridad = prioridad; }
    public void setPrioridadEtiqueta(String prioridadEtiqueta) { this.prioridadEtiqueta = prioridadEtiqueta; }
    public void setCategoria(Category categoria) { this.categoria = categoria; }
    public void setFechaLimite(LocalDate fechaLimite) { this.fechaLimite = fechaLimite; }
    public void setEstaVencida(boolean estaVencida) { this.estaVencida = estaVencida; }
    public void setDiasRestantes(long diasRestantes) { this.diasRestantes = diasRestantes; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }
    public void setFechaActualizacion(LocalDateTime fechaActualizacion) { this.fechaActualizacion = fechaActualizacion; }
}