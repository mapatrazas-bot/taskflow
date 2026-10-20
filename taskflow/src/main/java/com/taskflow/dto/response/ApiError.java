package com.taskflow.dto.response;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Estructura estándar de respuesta para errores de la API.
 *
 * <p><b>Concepto REST — Error response consistente:</b> todos los
 * errores de la API retornan el mismo formato JSON, facilitando
 * el manejo en el cliente. Incluye: timestamp, código HTTP,
 * mensaje legible, código de error y lista de campos inválidos.</p>
 *
 * <p>Ejemplo de respuesta 400 Bad Request:</p>
 * <pre>
 * {
 *   "timestamp": "2025-08-27T14:30:00",
 *   "status": 400,
 *   "error": "Validación fallida",
 *   "codigoError": "VALIDATION_ERROR",
 *   "path": "/api/tareas",
 *   "erroresDetalle": [
 *     {"campo": "titulo", "mensaje": "El título es obligatorio"},
 *     {"campo": "prioridad", "mensaje": "La prioridad es obligatoria"}
 *   ]
 * }
 * </pre>
 */
public class ApiError {

    private LocalDateTime timestamp;
    private int status;
    private String error;
    private String codigoError;
    private String path;
    private List<ErrorDetalle> erroresDetalle;

    // ── Constructor ───────────────────────────────────────────────────────────

    public ApiError(int status, String error, String codigoError, String path) {
        this.timestamp = LocalDateTime.now();
        this.status = status;
        this.error = error;
        this.codigoError = codigoError;
        this.path = path;
    }

    // ── Clase interna para errores de campo ───────────────────────────────────

    /**
     * Detalle de un error de validación en un campo específico.
     */
    public static class ErrorDetalle {
        private String campo;
        private String mensaje;

        public ErrorDetalle(String campo, String mensaje) {
            this.campo = campo;
            this.mensaje = mensaje;
        }

        public String getCampo() { return campo; }
        public String getMensaje() { return mensaje; }
    }

    // ── Getters y Setters ────────────────────────────────────────────────────

    public LocalDateTime getTimestamp() { return timestamp; }
    public int getStatus() { return status; }
    public String getError() { return error; }
    public String getCodigoError() { return codigoError; }
    public String getPath() { return path; }
    public List<ErrorDetalle> getErroresDetalle() { return erroresDetalle; }
    public void setErroresDetalle(List<ErrorDetalle> erroresDetalle) {
        this.erroresDetalle = erroresDetalle;
    }
}