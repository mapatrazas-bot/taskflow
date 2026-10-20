package com.taskflow.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Contrato de ordenamiento para colecciones de dominio.
 *
 * <p><b>Concepto Java — Interface con default methods:</b> define
 * los ordenamientos básicos como métodos abstractos y provee
 * el orden inverso como default method reutilizando la lógica.</p>
 *
 * <p><b>Concepto Java — Polimorfismo:</b> distintas clases pueden
 * implementar esta interfaz con criterios de ordenamiento diferentes.</p>
 *
 * @param <T> tipo de los elementos a ordenar
 */
public interface Sortable<T> {

    /**
     * Ordena los elementos por fecha de creación (más antigua primero).
     *
     * @param elementos lista de elementos a ordenar
     * @return nueva lista ordenada por fecha ascendente
     */
    List<T> ordenarPorFecha(List<T> elementos);

    /**
     * Ordena los elementos por prioridad (más urgente primero).
     *
     * @param elementos lista de elementos a ordenar
     * @return nueva lista ordenada por prioridad descendente
     */
    List<T> ordenarPorPrioridad(List<T> elementos);

    /**
     * Ordena por fecha de creación en orden inverso (más reciente primero).
     *
     * <p><b>Concepto Java — Default method que reutiliza otro método:</b>
     * en lugar de duplicar la lógica de ordenamiento, invierte el resultado
     * de {@code ordenarPorFecha()}.</p>
     *
     * @param elementos lista de elementos a ordenar
     * @return nueva lista ordenada por fecha descendente
     */
    default List<T> ordenarPorFechaDesc(List<T> elementos) {
        List<T> ordenada = new ArrayList<>(ordenarPorFecha(elementos));
        Collections.reverse(ordenada);
        return ordenada;
    }
}