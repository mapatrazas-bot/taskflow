package com.taskflow.controller;

import com.taskflow.domain.enums.Category;
import com.taskflow.domain.enums.Priority;
import com.taskflow.domain.enums.TaskStatus;
import com.taskflow.domain.model.Task;
import com.taskflow.domain.record.TaskFilter;
import com.taskflow.domain.record.TaskStats;
import com.taskflow.dto.request.ActualizarTareaRequest;
import com.taskflow.dto.request.CambiarEstadoRequest;
import com.taskflow.dto.request.CrearTareaRequest;
import com.taskflow.dto.response.ApiError;
import com.taskflow.dto.response.PageResponse;
import com.taskflow.dto.response.TareaResponse;
import com.taskflow.service.TaskJpaService;
import com.taskflow.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Controller REST completo para gestión de tareas.
 *
 * <p>Fase 3: endpoints en memoria (/api/tareas/*)</p>
 * <p>Fase 4: endpoints con BD real (/api/tareas/db/*)</p>
 */
@RestController
@RequestMapping("/api/tareas")
@Tag(name = "Tareas", description = "Operaciones CRUD completas sobre tareas")
public class TaskController {

    // Servicio en memoria — Fases 1-3 (legacy, se eliminará en Fase 5)
    private final TaskService taskService;

    // Servicio JPA — Fase 4 en adelante (BD real)
    private final TaskJpaService taskJpaService;

    public TaskController(TaskService taskService, TaskJpaService taskJpaService) {
        this.taskService = taskService;
        this.taskJpaService = taskJpaService;
    }

    // ═════════════════════════════════════════════════════════════════════════
    // ENDPOINTS EN MEMORIA — Fase 3 (legacy)
    // ═════════════════════════════════════════════════════════════════════════

    @Operation(summary = "Listar tareas (en memoria)", description = "Fase 3 — datos en memoria")
    @GetMapping
    public ResponseEntity<PageResponse<TareaResponse>> listarTareas(
            @Parameter(description = "Filtrar por estado")
            @RequestParam(required = false) String estado,
            @Parameter(description = "Filtrar por prioridad")
            @RequestParam(required = false) String prioridad,
            @Parameter(description = "Filtrar por categoría")
            @RequestParam(required = false) String categoria,
            @Parameter(description = "Texto en título o descripción")
            @RequestParam(required = false) String buscar,
            @Parameter(description = "Número de página (0-based)")
            @RequestParam(defaultValue = "0") int pagina,
            @Parameter(description = "Tamaño de página")
            @RequestParam(defaultValue = "10") int tamanio,
            @Parameter(description = "Ordenar por: fecha | prioridad")
            @RequestParam(defaultValue = "fecha") String ordenarPor) {

        TaskFilter filtro = new TaskFilter(
                resolverEstado(estado),
                resolverPrioridad(prioridad),
                resolverCategoria(categoria),
                Optional.ofNullable(buscar)
        );

        var pageResult = taskService.listarPaginado(pagina, tamanio, filtro);

        List<Task> contenidoOrdenado = "prioridad".equalsIgnoreCase(ordenarPor)
                ? taskService.ordenarPorPrioridad(pageResult.contenido())
                : taskService.ordenarPorFecha(pageResult.contenido());

        var pageResponse = PageResponse.from(
                new com.taskflow.domain.record.PageResult<>(
                        contenidoOrdenado.stream().map(TareaResponse::fromTask).toList(),
                        pageResult.paginaActual(),
                        pageResult.tamanioPagina(),
                        pageResult.totalElementos()
                )
        );

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Total-Count", String.valueOf(pageResult.totalElementos()));

        return ResponseEntity.ok().headers(headers).body(pageResponse);
    }

    @Operation(summary = "Obtener tarea por ID (en memoria)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tarea encontrada"),
            @ApiResponse(responseCode = "404", description = "Tarea no encontrada",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<TareaResponse> obtenerTarea(@PathVariable Long id) {
        return taskService.buscarPorId(id)
                .map(TareaResponse::fromTask)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new com.taskflow.domain.exception.TaskNotFoundException(id));
    }

    @Operation(summary = "Crear tarea (en memoria)")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Tarea creada"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @PostMapping
    public ResponseEntity<TareaResponse> crearTarea(
            @Valid @RequestBody CrearTareaRequest request) {
        Task tarea = new Task(
                request.getTitulo(),
                request.getPrioridad(),
                request.getCategoria(),
                request.getUsuarioId()
        );
        if (request.getDescripcion() != null) tarea.setDescripcion(request.getDescripcion());
        if (request.getFechaLimite() != null) tarea.setFechaLimite(request.getFechaLimite());

        Task creada = taskService.crear(tarea);
        return ResponseEntity.status(HttpStatus.CREATED).body(TareaResponse.fromTask(creada));
    }

    @Operation(summary = "Actualizar tarea completa (en memoria)")
    @PutMapping("/{id}")
    public ResponseEntity<TareaResponse> actualizarTarea(
            @PathVariable Long id,
            @Valid @RequestBody ActualizarTareaRequest request) {
        Task existente = taskService.obtenerPorId(id);
        existente.setTitulo(request.getTitulo());
        existente.setDescripcion(request.getDescripcion());
        existente.setPrioridad(request.getPrioridad());
        if (request.getFechaLimite() != null) existente.setFechaLimite(request.getFechaLimite());
        return ResponseEntity.ok(TareaResponse.fromTask(existente));
    }

    @Operation(summary = "Cambiar estado (en memoria)")
    @PatchMapping("/{id}/estado")
    public ResponseEntity<TareaResponse> cambiarEstado(
            @PathVariable Long id,
            @Valid @RequestBody CambiarEstadoRequest request) {
        Task actualizada = taskService.cambiarStatus(id, request.getNuevoEstado());
        return ResponseEntity.ok(TareaResponse.fromTask(actualizada));
    }

    @Operation(summary = "Eliminar tarea (en memoria)")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarTarea(@PathVariable Long id) {
        taskService.obtenerPorId(id);
        taskService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Estadísticas (en memoria)")
    @GetMapping("/estadisticas")
    public ResponseEntity<TaskStats> estadisticas() {
        return ResponseEntity.ok(taskService.calcularEstadisticas());
    }

    // ═════════════════════════════════════════════════════════════════════════
    // ENDPOINTS CON BASE DE DATOS — Fase 4 (JPA real)
    // ═════════════════════════════════════════════════════════════════════════

    @Operation(
            summary = "Listar tareas desde BD",
            description = "Fase 4 — paginación, filtros y sorting ejecutados en la base de datos"
    )
    @GetMapping("/db")
    public ResponseEntity<Page<TareaResponse>> listarDesdeDB(
            @RequestParam(required = false) TaskStatus estado,
            @RequestParam(required = false) Priority prioridad,
            @RequestParam(required = false) Category categoria,
            @RequestParam(required = false) String buscar,
            @RequestParam(defaultValue = "0")     int pagina,
            @RequestParam(defaultValue = "10")    int tamanio,
            @RequestParam(defaultValue = "fecha") String ordenarPor,
            @RequestParam(defaultValue = "asc")   String direccion) {

        Page<TareaResponse> page = taskJpaService.listar(
                estado, prioridad, categoria, buscar,
                pagina, tamanio, ordenarPor, direccion);

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Total-Count", String.valueOf(page.getTotalElements()));

        return ResponseEntity.ok().headers(headers).body(page);
    }

    @Operation(summary = "Obtener tarea por ID desde BD")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tarea encontrada"),
            @ApiResponse(responseCode = "404", description = "Tarea no encontrada",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @GetMapping("/db/{id}")
    public ResponseEntity<TareaResponse> obtenerDesdeDB(@PathVariable Long id) {
        return ResponseEntity.ok(taskJpaService.obtenerPorId(id));
    }

    @Operation(summary = "Crear tarea en BD")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Tarea creada en BD"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @PostMapping("/db")
    public ResponseEntity<TareaResponse> crearEnDB(
            @Valid @RequestBody CrearTareaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(taskJpaService.crear(request));
    }

    @Operation(summary = "Actualizar tarea completa en BD")
    @PutMapping("/db/{id}")
    public ResponseEntity<TareaResponse> actualizarEnDB(
            @PathVariable Long id,
            @Valid @RequestBody ActualizarTareaRequest request) {
        return ResponseEntity.ok(taskJpaService.actualizar(id, request));
    }

    @Operation(summary = "Cambiar estado en BD")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Estado actualizado"),
            @ApiResponse(responseCode = "409", description = "Transición inválida",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @PatchMapping("/db/{id}/estado")
    public ResponseEntity<TareaResponse> cambiarEstadoEnDB(
            @PathVariable Long id,
            @Valid @RequestBody CambiarEstadoRequest request) {
        return ResponseEntity.ok(taskJpaService.cambiarEstado(id, request));
    }

    @Operation(summary = "Eliminar tarea de BD")
    @DeleteMapping("/db/{id}")
    public ResponseEntity<Void> eliminarDeDB(@PathVariable Long id) {
        taskJpaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Estadísticas desde BD")
    @GetMapping("/db/estadisticas")
    public ResponseEntity<Map<String, Long>> estadisticasDB() {
        return ResponseEntity.ok(taskJpaService.estadisticas());
    }

    // ── Métodos privados de conversión de query params ────────────────────────

    private Optional<TaskStatus> resolverEstado(String estado) {
        return Optional.ofNullable(estado).flatMap(TaskStatus::desdeCadena);
    }

    private Optional<Priority> resolverPrioridad(String prioridad) {
        if (prioridad == null) return Optional.empty();
        try { return Optional.of(Priority.valueOf(prioridad.toUpperCase())); }
        catch (IllegalArgumentException e) { return Optional.empty(); }
    }

    private Optional<Category> resolverCategoria(String categoria) {
        if (categoria == null) return Optional.empty();
        try { return Optional.of(Category.valueOf(categoria.toUpperCase())); }
        catch (IllegalArgumentException e) { return Optional.empty(); }
    }
}