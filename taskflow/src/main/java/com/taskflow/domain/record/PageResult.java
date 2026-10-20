package com.taskflow.domain.record;

import java.util.List;

/**
 * Resultado paginado genérico.
 *
 * <p><b>Concepto Java — Generics en Records:</b> {@code <T>} permite que
 * este record sirva para paginar cualquier tipo de objeto:
 * {@code PageResult<Task>}, {@code PageResult<User>}, etc.
 * sin duplicar código.</p>
 *
 * <p><b>Concepto Java — Record con métodos derivados:</b> los métodos
 * {@code totalPaginas()}, {@code esPrimeraPagina()}, etc. son
 * calculados a partir de los campos del record.</p>
 *
 * @param <T> tipo de los elementos paginados
 */
public record PageResult<T>(
        List<T> contenido,
        int paginaActual,
        int tamanioPagina,
        long totalElementos
) {

    /**
     * Calcula el número total de páginas.
     * Usa {@code Math.ceil} para incluir una página parcial al final.
     *
     * @return total de páginas disponibles
     */
    public int totalPaginas() {
        if (tamanioPagina == 0) return 0;
        return (int) Math.ceil((double) totalElementos / tamanioPagina);
    }

    /**
     * @return {@code true} si esta es la primera página (índice 0)
     */
    public boolean esPrimeraPagina() {
        return paginaActual == 0;
    }

    /**
     * @return {@code true} si esta es la última página disponible
     */
    public boolean esUltimaPagina() {
        return paginaActual >= totalPaginas() - 1;
    }

    /**
     * @return {@code true} si no hay elementos en el contenido
     */
    public boolean estaVacia() {
        return contenido == null || contenido.isEmpty();
    }
}