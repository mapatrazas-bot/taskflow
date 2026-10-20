package com.taskflow.service;

import com.taskflow.domain.enums.Category;
import com.taskflow.domain.enums.Priority;
import com.taskflow.domain.enums.TaskStatus;
import com.taskflow.domain.exception.TaskNotFoundException;
import com.taskflow.domain.exception.TaskStateException;
import com.taskflow.dto.request.ActualizarTareaRequest;
import com.taskflow.dto.request.CambiarEstadoRequest;
import com.taskflow.dto.request.CrearTareaRequest;
import com.taskflow.dto.response.TareaResponse;
import com.taskflow.entity.TaskEntity;
import com.taskflow.entity.UserEntity;
import com.taskflow.repository.TaskRepository;
import com.taskflow.repository.TaskSpecification;
import com.taskflow.repository.TareaResumen;
import com.taskflow.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Servicio de tareas usando Spring Data JPA.
 *
 * <p>Reemplaza al {@code TaskService} de memoria de las fases anteriores.
 * Los datos ahora persisten en H2 (dev) o PostgreSQL (prod).</p>
 *
 * <p><b>Concepto Spring — @Service:</b> marca la clase como bean de
 * servicio. Spring la detecta por @ComponentScan y la registra en el
 * contenedor IoC.</p>
 *
 * <p><b>Concepto Spring — @Transactional:</b> garantiza que todas las
 * operaciones de BD dentro del método ocurran en una sola transacción.
 * Si algo falla, Hibernate hace rollback automático.</p>
 */
@Service
@Transactional(readOnly = true) // todas las operaciones son readOnly por defecto
public class TaskJpaService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    /**
     * Inyección por constructor — Spring resuelve los beans automáticamente.
     */
    public TaskJpaService(TaskRepository taskRepository,
                          UserRepository userRepository) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
    }

    // ── CRUD ─────────────────────────────────────────────────────────────────

    /**
     * Crea una nueva tarea y la persiste en la BD.
     *
     * <p><b>@Transactional</b> (sin readOnly): abre una transacción de
     * escritura. Hibernate hace flush al final del método.</p>
     */
    @Transactional
    public TareaResponse crear(CrearTareaRequest request) {
        UserEntity usuario = userRepository.findById(request.getUsuarioId())
                .orElseThrow(() -> new TaskNotFoundException("usuario", String.valueOf(request.getUsuarioId())));

        TaskEntity tarea = new TaskEntity(
                request.getTitulo(),
                request.getPrioridad(),
                request.getCategoria(),
                usuario
        );
        if (request.getDescripcion() != null) {
            tarea.setDescripcion(request.getDescripcion());
        }
        if (request.getFechaLimite() != null) {
            tarea.setFechaLimite(request.getFechaLimite());
        }

        TaskEntity guardada = taskRepository.save(tarea);
        return TareaResponse.fromTaskEntity(guardada);
    }

    /**
     * Busca una tarea por ID o lanza 404.
     */
    public TareaResponse obtenerPorId(Long id) {
        return taskRepository.findById(id)
                .map(TareaResponse::fromTaskEntity)
                .orElseThrow(() -> new TaskNotFoundException(id));
    }

    /**
     * Actualización completa con @Transactional.
     * Hibernate detecta los cambios (dirty checking) y genera el UPDATE automáticamente.
     */
    @Transactional
    public TareaResponse actualizar(Long id, ActualizarTareaRequest request) {
        TaskEntity tarea = taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException(id));

        tarea.setTitulo(request.getTitulo());
        tarea.setDescripcion(request.getDescripcion());
        tarea.setPrioridad(request.getPrioridad());
        tarea.setCategoria(request.getCategoria());
        if (request.getFechaLimite() != null) {
            tarea.setFechaLimite(request.getFechaLimite());
        }

        // No necesitamos llamar save() — Hibernate detecta el cambio (dirty checking)
        return TareaResponse.fromTaskEntity(tarea);
    }

    /**
     * Cambia el estado con validación de transición.
     */
    @Transactional
    public TareaResponse cambiarEstado(Long id, CambiarEstadoRequest request) {
        TaskEntity tarea = taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException(id));

        if (!tarea.getStatus().isActivo()) {
            throw new TaskStateException(tarea.getStatus(), request.getNuevoEstado());
        }

        tarea.setStatus(request.getNuevoEstado());
        return TareaResponse.fromTaskEntity(tarea);
    }

    /**
     * Elimina una tarea.
     */
    @Transactional
    public void eliminar(Long id) {
        if (!taskRepository.existsById(id)) {
            throw new TaskNotFoundException(id);
        }
        taskRepository.deleteById(id);
    }

    // ── Consultas con paginación real ─────────────────────────────────────────

    /**
     * Lista tareas con paginación, filtros dinámicos y sorting — todo en BD.
     *
     * <p><b>Concepto Spring Data — Pageable:</b> encapsula page, size y sort.
     * Spring Data genera automáticamente: LIMIT ? OFFSET ? ORDER BY ?</p>
     *
     * <p><b>Concepto Spring Data — Specification:</b> filtros dinámicos
     * que se traducen a predicados SQL WHERE.</p>
     */
    public Page<TareaResponse> listar(
            TaskStatus status, Priority prioridad,
            Category categoria, String buscar,
            int pagina, int tamanio, String ordenarPor, String direccion) {

        // Construir el Sort (ORDER BY) dinámico
        Sort.Direction dir = "desc".equalsIgnoreCase(direccion)
                ? Sort.Direction.DESC : Sort.Direction.ASC;

        // Mapear el campo de ordenamiento al nombre del campo en la entidad
        String campo = switch (ordenarPor) {
            case "prioridad" -> "prioridad";
            case "titulo"    -> "titulo";
            case "estado"    -> "status";
            default          -> "fechaCreacion";
        };

        // Pageable combina paginación + sorting
        Pageable pageable = PageRequest.of(pagina, tamanio, Sort.by(dir, campo));

        // Specification combina filtros dinámicos con AND
        var spec = TaskSpecification.construir(status, prioridad, categoria, buscar);

        // Una sola query a la BD con todo: WHERE + ORDER BY + LIMIT + OFFSET
        return taskRepository.findAll(spec, pageable)
                .map(TareaResponse::fromTaskEntity);
    }

    // ── Consultas especializadas ──────────────────────────────────────────────

    /** Retorna tareas vencidas usando @Query JPQL. */
    public List<TareaResponse> obtenerVencidas() {
        return taskRepository.findVencidas(
                        LocalDate.now(),
                        List.of(TaskStatus.PENDIENTE, TaskStatus.EN_PROGRESO))
                .stream()
                .map(TareaResponse::fromTaskEntity)
                .toList();
    }

    /** Estadísticas usando @Query de agrupación. */
    public Map<String, Long> estadisticas() {
        return taskRepository.contarPorEstado()
                .stream()
                .collect(Collectors.toMap(
                        row -> ((TaskStatus) row[0]).getEtiqueta(),
                        row -> (Long) row[1]
                ));
    }

    /** Proyección — lista resumida eficiente. */
    public List<TareaResumen> obtenerResumenUsuario(Long usuarioId) {
        return taskRepository.findResumenByUsuarioId(usuarioId);
    }
}