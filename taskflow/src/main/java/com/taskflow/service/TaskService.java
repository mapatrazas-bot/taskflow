package com.taskflow.service;

import com.taskflow.domain.enums.Category;
import com.taskflow.domain.enums.Priority;
import com.taskflow.domain.enums.TaskStatus;
import com.taskflow.domain.exception.TaskNotFoundException;
import com.taskflow.domain.model.Task;
import com.taskflow.domain.record.PageResult;
import com.taskflow.domain.record.TaskFilter;
import com.taskflow.domain.record.TaskStats;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * Servicio de tareas con almacenamiento en memoria.
 *
 * <p>En la Fase 1 no usamos base de datos ni Spring: este servicio
 * demuestra los conceptos Java puro con un {@code HashMap} como
 * almacén temporal.</p>
 *
 * <p><b>Concepto Java — implements múltiple de interfaces:</b>
 * implementa tanto {@code Filterable} como {@code Sortable},
 * demostrando polimorfismo a través de interfaces.</p>
 *
 * <p><b>Concepto Java — Collections:</b> {@code HashMap<Long, Task>}
 * como almacén en memoria, tipado con Generics.</p>
 *
 * <p><b>Concepto Java — Streams y Lambdas:</b> casi todas las consultas
 * usan la API de Streams para filtrar, ordenar y agrupar datos.</p>
 */
public class TaskService implements Filterable<Task, TaskFilter>, Sortable<Task> {

    /**
     * Almacén en memoria.
     * Concepto: Collections — HashMap tipado con Generics <Long, Task>.
     */
    private final Map<Long, Task> tareas = new HashMap<>();

    // ── CRUD ─────────────────────────────────────────────────────────────────

    /**
     * Guarda una tarea en el almacén.
     *
     * @param tarea tarea a guardar (ya tiene ID asignado desde BaseEntity)
     * @return la misma tarea guardada
     */
    public Task crear(Task tarea) {
        tareas.put(tarea.getId(), tarea);
        return tarea;
    }

    /**
     * Busca una tarea por ID de forma segura.
     *
     * <p><b>Concepto Java — Optional:</b> retornamos {@code Optional<Task>}
     * para que el llamador decida qué hacer si no existe.</p>
     *
     * @param id identificador de la tarea
     * @return {@code Optional} con la tarea, o vacío si no existe
     */
    public Optional<Task> buscarPorId(Long id) {
        return Optional.ofNullable(tareas.get(id));
    }

    /**
     * Retorna la tarea o lanza excepción si no existe.
     *
     * <p><b>Concepto Java — Optional.orElseThrow():</b> forma idiomática
     * de Java moderno para fallar rápido con mensaje contextual.</p>
     *
     * @param id identificador de la tarea
     * @return la tarea encontrada
     * @throws TaskNotFoundException si no existe una tarea con ese ID
     */
    public Task obtenerPorId(Long id) {
        return buscarPorId(id)
                .orElseThrow(() -> new TaskNotFoundException(id));
    }

    /**
     * Actualiza los campos de una tarea existente.
     *
     * @param id          ID de la tarea a actualizar
     * @param datosNuevos objeto con los nuevos valores
     * @return la tarea actualizada
     * @throws TaskNotFoundException si no existe
     */
    public Task actualizar(Long id, Task datosNuevos) {
        Task existente = obtenerPorId(id); // lanza si no existe
        existente.setTitulo(datosNuevos.getTitulo());
        existente.setDescripcion(datosNuevos.getDescripcion());
        existente.setPrioridad(datosNuevos.getPrioridad());
        datosNuevos.getFechaLimite().ifPresent(existente::setFechaLimite);
        return existente;
    }

    /**
     * Elimina una tarea del almacén.
     *
     * @param id ID de la tarea a eliminar
     * @throws TaskNotFoundException si no existe
     */
    public void eliminar(Long id) {
        obtenerPorId(id); // valida que exista antes de eliminar
        tareas.remove(id);
    }

    /**
     * Cambia el estado de una tarea aplicando las reglas de transición.
     *
     * @param id          ID de la tarea
     * @param nuevoStatus estado destino
     * @return la tarea con el estado actualizado
     * @throws TaskNotFoundException si no existe
     * @throws com.taskflow.domain.exception.TaskStateException si la transición no está permitida
     */
    public Task cambiarStatus(Long id, TaskStatus nuevoStatus) {
        Task tarea = obtenerPorId(id);
        tarea.cambiarStatus(nuevoStatus); // lanza TaskStateException si es inválido
        return tarea;
    }

    /**
     * Retorna todas las tareas como lista no modificable.
     *
     * <p><b>Concepto Java — Stream.toList() (Java 16+):</b> forma
     * concisa de obtener una lista inmutable desde un Stream.</p>
     *
     * @return lista inmutable de todas las tareas
     */
    public List<Task> listarTodas() {
        return tareas.values().stream().toList();
    }

    // ── Consultas con Streams ─────────────────────────────────────────────────

    /**
     * Retorna solo las tareas vencidas y sin resolver.
     *
     * <p><b>Concepto Java — Method reference:</b> {@code Task::estaVencida}
     * es equivalente a {@code t -> t.estaVencida()}, pero más conciso.</p>
     *
     * @return lista de tareas vencidas
     */
    public List<Task> tareasVencidas() {
        return tareas.values().stream()
                .filter(Task::estaVencida)
                .toList();
    }

    /**
     * Agrupa las tareas por categoría con conteo.
     *
     * <p><b>Concepto Java — Collectors.groupingBy + counting():</b>
     * operación de agrupación y agregación en un solo paso.</p>
     *
     * @return mapa de categoría → cantidad de tareas
     */
    public Map<Category, Long> contarPorCategoria() {
        return tareas.values().stream()
                .collect(Collectors.groupingBy(Task::getCategoria, Collectors.counting()));
    }

    /**
     * Agrupa las tareas por prioridad.
     *
     * <p><b>Concepto Java — Collectors.groupingBy():</b> retorna un
     * {@code Map<Priority, List<Task>>} donde cada prioridad tiene
     * su propia lista de tareas.</p>
     *
     * @return mapa de prioridad → lista de tareas con esa prioridad
     */
    public Map<Priority, List<Task>> agruparPorPrioridad() {
        return tareas.values().stream()
                .collect(Collectors.groupingBy(Task::getPrioridad));
    }

    /**
     * Calcula estadísticas de todas las tareas actuales.
     *
     * @return record {@code TaskStats} con todos los conteos
     */
    public TaskStats calcularEstadisticas() {
        return TaskStats.calcular(listarTodas());
    }

    /**
     * Lista tareas con paginación y filtrado.
     *
     * <p><b>Concepto Java — Stream encadenado con skip/limit:</b>
     * {@code skip()} y {@code limit()} implementan la lógica de paginación
     * sobre el Stream ya filtrado.</p>
     *
     * @param pagina    número de página (0-based)
     * @param tamanio   cantidad de elementos por página
     * @param filtro    criterios de filtrado
     * @return {@code PageResult<Task>} con el contenido paginado y metadata
     */
    public PageResult<Task> listarPaginado(int pagina, int tamanio, TaskFilter filtro) {
        List<Task> todasFiltradas = filtrar(listarTodas(), filtro);
        long totalElementos = todasFiltradas.size();

        List<Task> pagina_contenido = todasFiltradas.stream()
                .skip((long) pagina * tamanio)   // saltamos las páginas anteriores
                .limit(tamanio)                   // tomamos solo 'tamanio' elementos
                .collect(Collectors.toList());

        return new PageResult<>(pagina_contenido, pagina, tamanio, totalElementos);
    }

    // ── Implementación de Filterable<Task, TaskFilter> ────────────────────────

    /**
     * Filtra una lista de tareas usando los criterios del {@code TaskFilter}.
     *
     * <p><b>Concepto Java — Polimorfismo por interfaz:</b> este método
     * cumple el contrato de {@code Filterable}, pero la implementación
     * concreta es específica de {@code Task} y {@code TaskFilter}.</p>
     */
    @Override
    public List<Task> filtrar(List<Task> elementos, TaskFilter filtro) {
        if (!filtro.tieneAlgunFiltro()) {
            return elementos;
        }
        return elementos.stream()
                .filter(filtro::coincide)    // method reference al método del Record
                .collect(Collectors.toList());
    }

    // ── Implementación de Sortable<Task> ──────────────────────────────────────

    /**
     * Ordena por fecha de creación ascendente (más antigua primero).
     *
     * <p><b>Concepto Java — Comparator.comparing() con method reference:</b>
     * forma declarativa y type-safe de definir criterios de ordenamiento.</p>
     */
    @Override
    public List<Task> ordenarPorFecha(List<Task> elementos) {
        return elementos.stream()
                .sorted(Comparator.comparing(Task::getFechaCreacion))
                .collect(Collectors.toList());
    }

    /**
     * Ordena por nivel de prioridad descendente (URGENTE primero).
     *
     * <p><b>Concepto Java — Comparator.comparingInt().reversed():</b>
     * encadenamiento de Comparators para invertir el orden.</p>
     */
    @Override
    public List<Task> ordenarPorPrioridad(List<Task> elementos) {
        return elementos.stream()
                .sorted(Comparator.comparingInt(t -> -t.getPrioridad().getNivel()))
                .collect(Collectors.toList());
    }

    /** @return número total de tareas almacenadas */
    public int total() {
        return tareas.size();
    }

    /**
     * Aplica el filtro sobre todas las tareas del almacén.
     * Conveniencia que combina listarTodas() + filtrar().
     * Usado por el TaskController de la Fase 2.
     */
    public List<Task> buscarConFiltro(TaskFilter filtro) {
        return filtrar(listarTodas(), filtro);
    }
}