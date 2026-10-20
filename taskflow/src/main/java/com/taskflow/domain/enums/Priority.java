package com.taskflow.domain.enums;

import java.util.Comparator;

/**
 * Nivel de prioridad de una tarea, ordenado de menor a mayor urgencia.
 *
 * <p><b>Concepto Java — Enum con lógica de dominio:</b> los métodos
 * {@code esMayorQue} y {@code comparador()} demuestran que un enum
 * puede contener lógica de negocio real, no solo ser una constante.</p>
 */
public enum Priority {

    BAJA(1, "Baja"),
    MEDIA(2, "Media"),
    ALTA(3, "Alta"),
    URGENTE(4, "Urgente");

    /** Valor numérico para ordenamiento y comparación. */
    private final int nivel;
    private final String etiqueta;

    Priority(int nivel, String etiqueta) {
        this.nivel = nivel;
        this.etiqueta = etiqueta;
    }

    public int getNivel() {
        return nivel;
    }

    public String getEtiqueta() {
        return etiqueta;
    }

    /**
     * @return {@code true} si esta prioridad es URGENTE.
     */
    public boolean esUrgente() {
        return this == URGENTE;
    }

    /**
     * Compara esta prioridad con otra.
     *
     * @param otra la prioridad con la que comparar
     * @return {@code true} si esta prioridad es mayor que {@code otra}
     */
    public boolean esMayorQue(Priority otra) {
        return this.nivel > otra.nivel;
    }

    /**
     * Retorna un {@code Comparator} que ordena prioridades de mayor a menor.
     *
     * <p><b>Concepto Java — Comparator con lambda:</b> usamos
     * {@code Comparator.comparingInt} con una referencia a método
     * para definir el criterio de ordenamiento de forma declarativa.</p>
     *
     * @return comparador descendente por nivel de prioridad
     */
    public static Comparator<Priority> comparadorDescendente() {
        return Comparator.comparingInt(Priority::getNivel).reversed();
    }

    /**
     * Busca una prioridad por su nivel numérico.
     *
     * @param nivel valor entre 1 y 4
     * @return la prioridad correspondiente
     * @throws IllegalArgumentException si el nivel no existe
     */
    public static Priority desdeNivel(int nivel) {
        for (Priority p : values()) {
            if (p.nivel == nivel) {
                return p;
            }
        }
        throw new IllegalArgumentException("Nivel de prioridad inválido: " + nivel);
    }
}