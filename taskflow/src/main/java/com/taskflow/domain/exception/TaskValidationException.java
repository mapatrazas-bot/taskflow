package com.taskflow.domain.exception;

/**
 * Se lanza cuando los datos de una tarea no cumplen las reglas de negocio.
 *
 * <p><b>Concepto Java — Excepciones con contexto:</b> almacena el campo
 * específico que falló la validación, lo que permite mensajes de error
 * más útiles para el usuario final.</p>
 *
 * <p>Código de error: {@code "TASK_VALIDATION_ERROR"} · HTTP 400 Bad Request.</p>
 */
public class TaskValidationException extends ApiException {

    /** Nombre del campo que falló la validación. */
    private final String campoInvalido;

    /**
     * @param campoInvalido nombre del campo con el valor inválido (ej: "titulo")
     * @param mensaje       descripción del problema
     */
    public TaskValidationException(String campoInvalido, String mensaje) {
        super(
                String.format("Validación fallida en campo '%s': %s", campoInvalido, mensaje),
                "TASK_VALIDATION_ERROR",
                400
        );
        this.campoInvalido = campoInvalido;
    }

    /** @return nombre del campo que no pasó la validación */
    public String getCampoInvalido() {
        return campoInvalido;
    }
}