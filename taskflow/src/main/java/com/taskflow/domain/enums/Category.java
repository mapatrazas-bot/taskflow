package com.taskflow.domain.enums;

import java.util.Arrays;
import java.util.List;

/**
 * Categoría temática a la que pertenece una tarea.
 *
 * <p><b>Concepto Java — Enum básico con campo:</b> demuestra que incluso
 * los enums simples pueden tener datos adicionales por constante.</p>
 *
 * <p><b>Concepto Java — Generics + Collections:</b> el método {@code todas()}
 * retorna {@code List<Category>} tipada, evitando casts innecesarios.</p>
 */
public enum Category {

    PERSONAL("Personal - actividades de la vida diaria"),
    TRABAJO("Trabajo - tareas laborales y profesionales"),
    ESTUDIO("Estudio - aprendizaje y formación académica"),
    OTRO("Otro - categoría general");

    private final String descripcion;

    Category(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }

    /**
     * Retorna todas las categorías disponibles como lista.
     *
     * <p><b>Concepto Java — Generics + Collections:</b>
     * {@code Arrays.asList(values())} convierte el array de constantes
     * en una {@code List<Category>} tipada.</p>
     *
     * @return lista inmutable de todas las categorías
     */
    public static List<Category> todas() {
        return Arrays.asList(values());
    }
}