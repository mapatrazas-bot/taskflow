package com.taskflow.dto.request;

import com.taskflow.domain.enums.Category;
import com.taskflow.domain.enums.Priority;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/**
 * DTO para actualización completa de una tarea (PUT).
 *
 * <p><b>Concepto REST — PUT vs PATCH:</b>
 * PUT reemplaza el recurso completo — todos los campos son obligatorios.
 * PATCH actualiza parcialmente — solo los campos enviados cambian.
 * Aquí usamos PUT con todos los campos obligatorios excepto fechaLimite.</p>
 */
public class ActualizarTareaRequest {

    @NotBlank(message = "El título es obligatorio")
    @Size(min = 3, max = 100, message = "El título debe tener entre 3 y 100 caracteres")
    private String titulo;

    @Size(max = 500, message = "La descripción no puede superar 500 caracteres")
    private String descripcion;

    @NotNull(message = "La prioridad es obligatoria")
    private Priority prioridad;

    @NotNull(message = "La categoría es obligatoria")
    private Category categoria;

    @Future(message = "La fecha límite debe ser una fecha futura")
    private LocalDate fechaLimite;

    public ActualizarTareaRequest() {}

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
}