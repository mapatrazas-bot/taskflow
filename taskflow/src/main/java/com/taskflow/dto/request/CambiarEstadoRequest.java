package com.taskflow.dto.request;

import com.taskflow.domain.enums.TaskStatus;
import jakarta.validation.constraints.NotNull;

/**
 * DTO para actualización parcial del estado de una tarea (PATCH).
 *
 * <p><b>Concepto REST — PATCH:</b> actualiza solo un campo específico
 * sin requerir el recurso completo. Ideal para cambios de estado
 * donde no queremos enviar todos los campos de la tarea.</p>
 */
public class CambiarEstadoRequest {

    @NotNull(message = "El nuevo estado es obligatorio")
    private TaskStatus nuevoEstado;

    public CambiarEstadoRequest() {}
    public CambiarEstadoRequest(TaskStatus nuevoEstado) {
        this.nuevoEstado = nuevoEstado;
    }

    public TaskStatus getNuevoEstado() { return nuevoEstado; }
    public void setNuevoEstado(TaskStatus nuevoEstado) { this.nuevoEstado = nuevoEstado; }
}