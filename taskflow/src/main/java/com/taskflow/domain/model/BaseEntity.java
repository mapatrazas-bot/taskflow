package com.taskflow.domain.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Clase base abstracta para todas las entidades del dominio.
 *
 * <p><b>Concepto Java — Herencia:</b> {@code Task} y {@code User} extienden
 * esta clase y heredan automáticamente {@code id}, {@code fechaCreacion}
 * y {@code fechaActualizacion}, eliminando duplicación de código.</p>
 *
 * <p><b>Concepto Java — Clase abstracta vs interfaz:</b> usamos
 * {@code abstract class} (no interfaz) porque queremos compartir
 * estado (campos) y comportamiento concreto entre subclases.
 * Las interfaces no pueden tener estado de instancia.</p>
 *
 * <p><b>Concepto Java — LocalDateTime:</b> fecha y hora sin zona horaria,
 * suficiente para un sistema que opera en una sola zona.</p>
 */
public abstract class BaseEntity {

    /** Generador de IDs único por JVM, thread-safe. */
    private static final AtomicLong CONTADOR_ID = new AtomicLong(1);

    private static final DateTimeFormatter FORMATO_DISPLAY =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final Long id;
    private final LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;

    /**
     * Constructor protegido: solo accesible desde subclases.
     * Asigna ID automático y registra la fecha/hora actual.
     */
    protected BaseEntity() {
        this.id = CONTADOR_ID.getAndIncrement();
        this.fechaCreacion = LocalDateTime.now();
        this.fechaActualizacion = LocalDateTime.now();
    }

    // ── Getters públicos ─────────────────────────────────────────────────────

    public Long getId() {
        return id;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public LocalDateTime getFechaActualizacion() {
        return fechaActualizacion;
    }

    // ── Métodos concretos ────────────────────────────────────────────────────

    /**
     * Verifica si la entidad fue creada en las últimas 24 horas.
     *
     * <p><b>Concepto Java — LocalDateTime:</b> usamos {@code LocalDateTime.now()}
     * y {@code minusHours(24)} para calcular el rango de tiempo.</p>
     *
     * @return {@code true} si fue creada hace menos de 24 horas
     */
    public boolean esReciente() {
        return fechaCreacion.isAfter(LocalDateTime.now().minusHours(24));
    }

    /**
     * Actualiza la marca de tiempo de modificación al instante actual.
     * Solo accesible desde subclases (encapsulamiento).
     */
    protected void marcarActualizado() {
        this.fechaActualizacion = LocalDateTime.now();
    }

    // ── Método abstracto ─────────────────────────────────────────────────────

    /**
     * Retorna una descripción corta de la entidad para logs y debug.
     *
     * <p><b>Concepto Java — Método abstracto:</b> obliga a cada subclase
     * a proveer su propia implementación. El compilador rechaza una subclase
     * concreta que no lo implemente.</p>
     *
     * @return descripción corta en una sola línea
     */
    public abstract String getDescripcionCorta();

    // ── Object overrides ─────────────────────────────────────────────────────

    /**
     * Concepto Java — Polimorfismo: toString() sobreescrito para mostrar
     * información básica de cualquier entidad sin conocer su tipo concreto.
     */
    @Override
    public String toString() {
        return String.format("[%s#%d | creado: %s]",
                getClass().getSimpleName(),
                id,
                fechaCreacion.format(FORMATO_DISPLAY));
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        BaseEntity other = (BaseEntity) obj;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return Long.hashCode(id != null ? id : 0L);
    }
}