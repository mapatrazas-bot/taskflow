package com.taskflow.dto.response;

import com.taskflow.domain.record.PageResult;

import java.util.List;

/**
 * DTO de respuesta paginada para listas de recursos.
 *
 * <p><b>Concepto REST — Paginación:</b> en lugar de retornar todos los
 * recursos de golpe, retornamos un subconjunto con metadatos que
 * permiten al cliente navegar entre páginas.</p>
 *
 * <p>Ejemplo de respuesta:</p>
 * <pre>
 * {
 *   "contenido": [...],
 *   "paginaActual": 0,
 *   "tamanioPagina": 2,
 *   "totalElementos": 5,
 *   "totalPaginas": 3,
 *   "esPrimeraPagina": true,
 *   "esUltimaPagina": false
 * }
 * </pre>
 */
public class PageResponse<T> {

    private List<T> contenido;
    private int paginaActual;
    private int tamanioPagina;
    private long totalElementos;
    private int totalPaginas;
    private boolean esPrimeraPagina;
    private boolean esUltimaPagina;

    public PageResponse() {}

    /**
     * Factory method — construye desde el record PageResult de Fase 1.
     */
    public static <T> PageResponse<T> from(PageResult<T> pageResult) {
        PageResponse<T> response = new PageResponse<>();
        response.contenido = pageResult.contenido();
        response.paginaActual = pageResult.paginaActual();
        response.tamanioPagina = pageResult.tamanioPagina();
        response.totalElementos = pageResult.totalElementos();
        response.totalPaginas = pageResult.totalPaginas();
        response.esPrimeraPagina = pageResult.esPrimeraPagina();
        response.esUltimaPagina = pageResult.esUltimaPagina();
        return response;
    }

    public List<T> getContenido() { return contenido; }
    public int getPaginaActual() { return paginaActual; }
    public int getTamanioPagina() { return tamanioPagina; }
    public long getTotalElementos() { return totalElementos; }
    public int getTotalPaginas() { return totalPaginas; }
    public boolean isEsPrimeraPagina() { return esPrimeraPagina; }
    public boolean isEsUltimaPagina() { return esUltimaPagina; }
}