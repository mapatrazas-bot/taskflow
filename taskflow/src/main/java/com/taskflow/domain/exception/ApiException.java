package com.taskflow.domain.exception;

/**
 * Excepción base de toda la aplicación TaskFlow.
 *
 * <p><b>Concepto Java — Jerarquía de excepciones:</b> crear una excepción
 * raíz propia permite capturar cualquier error de la aplicación con un solo
 * {@code catch (ApiException e)}, mientras que las subclases añaden contexto
 * específico (tarea no encontrada, estado inválido, etc.).</p>
 *
 * <p><b>Concepto Java — Unchecked exception:</b> extiende
 * {@code RuntimeException}, por lo que el compilador NO obliga al llamador
 * a declarar {@code throws ApiException}. Ideal para errores de dominio
 * que son responsabilidad del llamador (no de recursos externos).</p>
 *
 * <p>Cada subclase define su propio {@code codigoError} (legible por máquinas)
 * y {@code httpStatus} (para cuando se integre con Spring en Fase 3).</p>
 */
public abstract class ApiException extends RuntimeException {

    /** Código de error legible por máquinas. Ej: "TASK_NOT_FOUND". */
    private final String codigoError;

    /**
     * Código de estado HTTP equivalente.
     * Se usa en Fase 3 cuando integremos Spring MVC.
     * Ej: 404 → Not Found, 409 → Conflict.
     */
    private final int httpStatus;

    /**
     * Constructor principal.
     *
     * @param mensaje    descripción legible del error
     * @param codigoError código corto de error (snake_case en mayúsculas)
     * @param httpStatus código HTTP equivalente
     */
    protected ApiException(String mensaje, String codigoError, int httpStatus) {
        super(mensaje);
        this.codigoError = codigoError;
        this.httpStatus = httpStatus;
    }

    /**
     * Constructor con causa raíz, para encadenar excepciones.
     *
     * <p><b>Concepto Java — Exception chaining:</b> pasar la excepción
     * original como {@code causa} preserva el stack trace completo,
     * facilitando el diagnóstico.</p>
     *
     * @param mensaje    descripción legible del error
     * @param causa      excepción que originó este error
     * @param codigoError código corto de error
     * @param httpStatus código HTTP equivalente
     */
    protected ApiException(String mensaje, Throwable causa, String codigoError, int httpStatus) {
        super(mensaje, causa);
        this.codigoError = codigoError;
        this.httpStatus = httpStatus;
    }

    /** @return código de error corto, legible por máquinas */
    public String getCodigoError() {
        return codigoError;
    }

    /** @return código de estado HTTP equivalente a este error */
    public int getHttpStatus() {
        return httpStatus;
    }
}