package com.taskflow.domain.enums;

import java.util.Optional;

/**
 * Define los estados posibles del ciclo de vida de una tarea.
 *
 * <p><b>Concepto Java — Enum con comportamiento:</b> un enum no es solo una lista
 * de constantes; puede tener campos, constructores y métodos como cualquier clase.</p>
 *
 * <p><b>Concepto Java — Optional:</b> el método {@code desdeCadena} retorna
 * {@code Optional<TaskStatus>} en lugar de {@code null} para forzar al
 * llamador a manejar explícitamente el caso "no encontrado".</p>
 */
public enum TaskStatus {

    PENDIENTE("Pendiente", true),
    EN_PROGRESO("En progreso", true),
    COMPLETADA("Completada", false),
    CANCELADA("Cancelada", false);

    /** Etiqueta legible en español para mostrar en interfaces. */
    private final String etiqueta;

    /**
     * Indica si la tarea aún puede ser modificada.
     * {@code false} significa estado final: no se aceptan más cambios.
     */
    private final boolean activo;

    /**
     * Constructor del enum. Se ejecuta una vez por cada constante declarada.
     *
     * @param etiqueta nombre legible en español
     * @param activo   {@code true} si el estado permite modificaciones posteriores
     */
    TaskStatus(String etiqueta, boolean activo) {
        this.etiqueta = etiqueta;
        this.activo = activo;
    }

    /** @return etiqueta legible en español */
    public String getEtiqueta() {
        return etiqueta;
    }

    /**
     * @return {@code true} si desde este estado la tarea puede cambiar.
     *         {@code false} para COMPLETADA y CANCELADA (estados finales).
     */
    public boolean isActivo() {
        return activo;
    }

    /**
     * Busca un {@code TaskStatus} por nombre, ignorando mayúsculas.
     *
     * <p><b>Concepto Java — Optional:</b> en lugar de retornar {@code null}
     * cuando el valor no existe, retornamos {@code Optional.empty()}.
     * Esto elimina la posibilidad de un {@code NullPointerException}
     * accidental en el llamador.</p>
     *
     * @param nombre nombre del estado (ej: "pendiente", "COMPLETADA")
     * @return {@code Optional} con el estado encontrado, o vacío si no existe
     */
    public static Optional<TaskStatus> desdeCadena(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            return Optional.empty();
        }
        for (TaskStatus estado : values()) {
            if (estado.name().equalsIgnoreCase(nombre.trim())) {
                return Optional.of(estado);
            }
        }
        return Optional.empty();
    }
}