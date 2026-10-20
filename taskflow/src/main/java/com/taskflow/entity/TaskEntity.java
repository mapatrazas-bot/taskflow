package com.taskflow.entity;

import com.taskflow.domain.enums.Category;
import com.taskflow.domain.enums.Priority;
import com.taskflow.domain.enums.TaskStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * Entidad JPA que mapea la tabla {@code tasks}.
 *
 * <p><b>Concepto JPA — @Enumerated(EnumType.STRING):</b>
 * persiste el enum como texto ("PENDIENTE") en lugar de su índice (0).
 * Esto hace la BD legible y evita problemas si el orden del enum cambia.</p>
 *
 * <p><b>Concepto JPA — @ManyToOne:</b>
 * muchas tareas pertenecen a un usuario.
 * FetchType.LAZY: el usuario NO se carga hasta que se accede a él.</p>
 *
 * <p><b>Concepto JPA — @JoinColumn:</b>
 * especifica la columna de clave foránea en la tabla tasks.</p>
 */
@Entity
@Table(name = "tasks")
@EntityListeners(AuditingEntityListener.class)
public class TaskEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "tasks_gen")
    @SequenceGenerator(name = "tasks_gen", sequenceName = "tasks_seq", allocationSize = 1)
    private Long id;

    @Column(nullable = false, length = 100)
    private String titulo;

    @Column(length = 500)
    private String descripcion;

    /**
     * Concepto JPA — @Enumerated(EnumType.STRING):
     * Guarda "PENDIENTE", "EN_PROGRESO", etc. en la BD.
     * No usar EnumType.ORDINAL — si reordenamos el enum, los datos se corrompen.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TaskStatus status = TaskStatus.PENDIENTE;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Priority prioridad;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Category categoria;

    @Column(name = "fecha_limite")
    private LocalDate fechaLimite;

    /**
     * Concepto JPA — Relación @ManyToOne:
     * El lado "muchos" de la relación User → Task.
     * @JoinColumn especifica la columna FK en la tabla tasks.
     * LAZY: no carga el usuario hasta que se accede a usuario.getNombre(), etc.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private UserEntity usuario;

    @CreatedDate
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @LastModifiedDate
    @Column(name = "fecha_actualizacion", nullable = false)
    private LocalDateTime fechaActualizacion;

    // ── Constructor vacío — requerido por JPA ────────────────────────────────
    public TaskEntity() {}

    public TaskEntity(String titulo, Priority prioridad,
                      Category categoria, UserEntity usuario) {
        this.titulo = titulo;
        this.prioridad = prioridad;
        this.categoria = categoria;
        this.usuario = usuario;
    }

    // ── Métodos de negocio ───────────────────────────────────────────────────

    /** Indica si la tarea está vencida y sigue activa. */
    public boolean estaVencida() {
        return fechaLimite != null
                && fechaLimite.isBefore(LocalDate.now())
                && status.isActivo();
    }

    /** Días restantes hasta la fecha límite (negativo si vencida). */
    public long diasRestantes() {
        if (fechaLimite == null) return 0L;
        return ChronoUnit.DAYS.between(LocalDate.now(), fechaLimite);
    }

    // ── Ciclo de vida JPA (Garantiza fechas no nulas en tests y producción) ──
    @PrePersist
    public void prePersist() {
        if (fechaCreacion == null) {
            fechaCreacion = LocalDateTime.now();
        }
        if (fechaActualizacion == null) {
            fechaActualizacion = LocalDateTime.now();
        }
    }

    @PreUpdate
    public void preUpdate() {
        fechaActualizacion = LocalDateTime.now();
    }

    // ── Getters y Setters ────────────────────────────────────────────────────
    public Long getId() { return id; }
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public TaskStatus getStatus() { return status; }
    public void setStatus(TaskStatus status) { this.status = status; }
    public Priority getPrioridad() { return prioridad; }
    public void setPrioridad(Priority prioridad) { this.prioridad = prioridad; }
    public Category getCategoria() { return categoria; }
    public void setCategoria(Category categoria) { this.categoria = categoria; }
    public LocalDate getFechaLimite() { return fechaLimite; }
    public void setFechaLimite(LocalDate fechaLimite) { this.fechaLimite = fechaLimite; }
    public UserEntity getUsuario() { return usuario; }
    public void setUsuario(UserEntity usuario) { this.usuario = usuario; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public LocalDateTime getFechaActualizacion() { return fechaActualizacion; }
}