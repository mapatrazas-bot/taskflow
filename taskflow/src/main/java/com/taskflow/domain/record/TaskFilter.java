package com.taskflow.domain.record;

import com.taskflow.domain.enums.Category;
import com.taskflow.domain.enums.Priority;
import com.taskflow.domain.enums.TaskStatus;
import com.taskflow.domain.model.Task;

import java.util.Optional;

/**
 * Criterios de búsqueda y filtrado de tareas.
 *
 * <p><b>Concepto Java — Record (Java 16+):</b> un {@code record} es una clase
 * inmutable que el compilador genera automáticamente con:
 * constructor canónico, getters, {@code equals()}, {@code hashCode()}
 * y {@code toString()}. No hay setters porque los records son inmutables.</p>
 *
 * <p><b>¿Por qué un Record y no una clase normal?</b>
 * Un filtro es un dato de entrada puro: se crea, se usa para filtrar,
 * y no necesita mutar. Los Records son la elección idiomática de Java moderno
 * para este tipo de "data carriers".</p>
 *
 * <p><b>Concepto Java — Optional en campos:</b> cada criterio es
 * {@code Optional<T>} porque puede o no estar presente.
 * Un {@code Optional.empty()} significa "sin restricción en este campo".</p>
 */
public record TaskFilter(
        Optional<TaskStatus> status,
        Optional<Priority> prioridad,
        Optional<Category> categoria,
        Optional<String> textoBusqueda
) {

    /**
     * Factory method: filtro vacío que retorna todas las tareas sin restricción.
     *
     * <p><b>Concepto Java — método estático en Record:</b> los records
     * pueden tener métodos estáticos y de instancia como cualquier clase.</p>
     *
     * @return filtro con todos los criterios vacíos
     */
    public static TaskFilter sinFiltros() {
        return new TaskFilter(
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty()
        );
    }

    /**
     * Factory method: filtrar solo por estado.
     *
     * @param estado estado de tarea a filtrar
     * @return filtro con solo el estado activo
     */
    public static TaskFilter porEstado(TaskStatus estado) {
        return new TaskFilter(
                Optional.of(estado),
                Optional.empty(),
                Optional.empty(),
                Optional.empty()
        );
    }

    /**
     * Factory method: filtrar solo por categoría.
     *
     * @param categoria categoría a filtrar
     * @return filtro con solo la categoría activa
     */
    public static TaskFilter porCategoria(Category categoria) {
        return new TaskFilter(
                Optional.empty(),
                Optional.empty(),
                Optional.of(categoria),
                Optional.empty()
        );
    }

    /**
     * Verifica si al menos un criterio de filtrado está activo.
     *
     * <p><b>Concepto Java — Optional.isPresent():</b> verifica si el
     * Optional contiene un valor.</p>
     *
     * @return {@code true} si hay al menos un criterio de filtrado
     */
    public boolean tieneAlgunFiltro() {
        return status.isPresent()
                || prioridad.isPresent()
                || categoria.isPresent()
                || textoBusqueda.isPresent();
    }

    /**
     * Evalúa si una tarea concreta coincide con todos los criterios activos.
     *
     * <p><b>Concepto Java — Optional.map() + orElse():</b>
     * si el Optional tiene valor, aplicamos la condición; si está vacío,
     * consideramos que el criterio se cumple (sin restricción).</p>
     *
     * @param tarea la tarea a evaluar
     * @return {@code true} si la tarea cumple todos los criterios activos
     */
    public boolean coincide(Task tarea) {
        // Verifica estado (si el filtro de estado está activo)
        boolean cumpleEstado = status
                .map(s -> tarea.getStatus() == s)
                .orElse(true); // sin filtro → pasa siempre

        // Verifica prioridad
        boolean cumplePrioridad = prioridad
                .map(p -> tarea.getPrioridad() == p)
                .orElse(true);

        // Verifica categoría
        boolean cumpleCategoria = categoria
                .map(c -> tarea.getCategoria() == c)
                .orElse(true);

        // Verifica texto en título o descripción (insensible a mayúsculas)
        boolean cumpleTexto = textoBusqueda
                .map(texto -> {
                    String buscar = texto.toLowerCase();
                    boolean enTitulo = tarea.getTitulo().toLowerCase().contains(buscar);
                    boolean enDesc = tarea.getDescripcion() != null
                            && tarea.getDescripcion().toLowerCase().contains(buscar);
                    return enTitulo || enDesc;
                })
                .orElse(true);

        return cumpleEstado && cumplePrioridad && cumpleCategoria && cumpleTexto;
    }
}