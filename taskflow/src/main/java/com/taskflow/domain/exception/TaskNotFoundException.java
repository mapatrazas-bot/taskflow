package com.taskflow.domain.exception;

/**
 * Se lanza cuando se busca una tarea que no existe en el sistema.
 *
 * <p><b>Concepto Java — Herencia en excepciones:</b> extiende
 * {@code ApiException} y añade semántica específica de "no encontrado".
 * Siempre usa el código {@code "TASK_NOT_FOUND"} y HTTP 404.</p>
 */
public class TaskNotFoundException extends ApiException {

    private static final String CODIGO = "TASK_NOT_FOUND";
    private static final int HTTP_STATUS = 404;

    /**
     * Excepción al buscar por ID numérico.
     *
     * @param id identificador de la tarea no encontrada
     */
    public TaskNotFoundException(Long id) {
        super(
                String.format("Tarea con ID %d no encontrada.", id),
                CODIGO,
                HTTP_STATUS
        );
    }

    /**
     * Excepción al buscar por cualquier criterio arbitrario.
     * Útil para búsquedas por título, estado, etc.
     *
     * @param criterio nombre del campo buscado (ej: "título")
     * @param valor    valor que no se encontró
     */
    public TaskNotFoundException(String criterio, String valor) {
        super(
                String.format("No se encontró ninguna tarea con %s = '%s'.", criterio, valor),
                CODIGO,
                HTTP_STATUS
        );
    }
}