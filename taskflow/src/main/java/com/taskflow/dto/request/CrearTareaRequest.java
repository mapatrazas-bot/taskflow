package com.taskflow.dto.request;

import com.taskflow.domain.enums.Category;
import com.taskflow.domain.enums.Priority;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/**
 * DTO de entrada para crear una nueva tarea.
 *
 * <p><b>Concepto Spring — DTO (Data Transfer Object):</b> clase que
 * solo transporta datos entre el cliente y el servidor. NO es la
 * entidad de dominio {@code Task} — separa la representación HTTP
 * de la lógica interna. Beneficio: podemos cambiar la API sin
 * afectar el modelo de dominio y viceversa.</p>
 *
 * <p><b>Concepto Spring — Bean Validation:</b> las anotaciones como
 * {@code @NotBlank}, {@code @Size}, {@code @NotNull} son de la
 * especificación Jakarta Validation. Spring las evalúa automáticamente
 * cuando el controller usa {@code @Valid} en el parámetro.</p>
 */
public class CrearTareaRequest {

    /**
     * @NotBlank — no puede ser null, vacío ni solo espacios.
     * @Size — longitud entre 3 y 100 caracteres.
     * El message es el texto que aparece en el error de validación.
     */
    @NotBlank(message = "El título es obligatorio")
    @Size(min = 3, max = 100, message = "El título debe tener entre 3 y 100 caracteres")
    private String titulo;

    @Size(max = 500, message = "La descripción no puede superar 500 caracteres")
    private String descripcion; // opcional — sin @NotBlank

    @NotNull(message = "La prioridad es obligatoria")
    private Priority prioridad;

    @NotNull(message = "La categoría es obligatoria")
    private Category categoria;

    /**
     * @Future — la fecha límite debe ser en el futuro.
     * No es obligatoria — puede ser null (tarea sin vencimiento).
     */
    @Future(message = "La fecha límite debe ser una fecha futura")
    private LocalDate fechaLimite; // opcional

    @NotNull(message = "El ID del usuario es obligatorio")
    private Long usuarioId;

    // ── Constructores ────────────────────────────────────────────────────────

    public CrearTareaRequest() {}

    public CrearTareaRequest(String titulo, Priority prioridad,
                             Category categoria, Long usuarioId) {
        this.titulo = titulo;
        this.prioridad = prioridad;
        this.categoria = categoria;
        this.usuarioId = usuarioId;
    }

    // ── Getters y Setters ────────────────────────────────────────────────────

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public Priority getPrioridad() { return prioridad; }
    public void setPrioridad(Priority prioridad) { this.prioridad = prioridad; }

    public Category getCategoria() { return categoria; }
    public void setCategoria(Category categoria) { this.categoria = categoria; }

    public LocalDate getFechaLimite() { return fechaLimite; }
    public void setFechaLimite(LocalDate fechaLimite) { this.fechaLimite = fechaLimite; }

    public Long getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Long usuarioId) { this.usuarioId = usuarioId; }
}