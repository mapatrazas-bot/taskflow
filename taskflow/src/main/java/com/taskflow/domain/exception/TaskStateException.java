package com.taskflow.domain.exception;

import com.taskflow.domain.enums.TaskStatus;

/**
 * Se lanza cuando se intenta una transición de estado inválida.
 *
 * <p>Ejemplo: intentar cambiar el estado de una tarea que ya está
 * {@code COMPLETADA} o {@code CANCELADA} (estados finales).</p>
 *
 * <p>Código de error: {@code "TASK_STATE_ERROR"} · HTTP 409 Conflict.</p>
 */
public class TaskStateException extends ApiException {

    private final TaskStatus estadoActual;
    private final TaskStatus estadoIntentado;

    /**
     * @param estadoActual    estado en el que se encuentra la tarea
     * @param estadoIntentado estado al que se intentó cambiar
     */
    public TaskStateException(TaskStatus estadoActual, TaskStatus estadoIntentado) {
        super(
                String.format(
                        "No se puede cambiar el estado de '%s' a '%s'. " +
                                "El estado '%s' es final y no acepta más cambios.",
                        estadoActual.getEtiqueta(),
                        estadoIntentado.getEtiqueta(),
                        estadoActual.getEtiqueta()
                ),
                "TASK_STATE_ERROR",
                409
        );
        this.estadoActual = estadoActual;
        this.estadoIntentado = estadoIntentado;
    }

    public TaskStatus getEstadoActual() {
        return estadoActual;
    }

    public TaskStatus getEstadoIntentado() {
        return estadoIntentado;
    }
}