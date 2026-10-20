package com.taskflow.service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Contrato de filtrado genérico para colecciones de dominio.
 *
 * <p><b>Concepto Java — Interface:</b> define un contrato sin implementación.
 * Cualquier clase que implemente {@code Filterable<T, F>} debe proveer
 * el método {@code filtrar()}, pero puede hacerlo de forma diferente.</p>
 *
 * <p><b>Concepto Java — Generics en interfaces:</b> {@code T} es el tipo
 * de elemento (ej: {@code Task}), {@code F} es el tipo del filtro
 * (ej: {@code TaskFilter}).</p>
 *
 * <p><b>Concepto Java — Default methods (Java 8+):</b> las interfaces
 * pueden tener métodos con implementación por defecto. Esto permite
 * añadir comportamiento a la interfaz sin romper las implementaciones
 * existentes.</p>
 *
 * @param <T> tipo de los elementos a filtrar
 * @param <F> tipo del objeto filtro
 */
public interface Filterable<T, F> {

    /**
     * Filtra una lista de elementos según los criterios del filtro.
     *
     * @param elementos lista de elementos a filtrar
     * @param filtro    criterios de búsqueda
     * @return lista filtrada (nunca {@code null}, puede estar vacía)
     */
    List<T> filtrar(List<T> elementos, F filtro);

    /**
     * Filtra y ordena en una sola operación.
     *
     * <p><b>Concepto Java — Default method:</b> implementación por defecto
     * que reutiliza {@code filtrar()} y le agrega ordenamiento.
     * Las subclases pueden sobreescribirlo si necesitan otro comportamiento.</p>
     *
     * @param elementos   lista de elementos
     * @param filtro      criterios de búsqueda
     * @param comparador  criterio de ordenamiento
     * @return lista filtrada y ordenada
     */
    default List<T> filtrarYOrdenar(List<T> elementos, F filtro, Comparator<T> comparador) {
        return filtrar(elementos, filtro)
                .stream()
                .sorted(comparador)
                .collect(Collectors.toList());
    }
}