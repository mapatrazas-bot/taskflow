package com.taskflow.repository;

import com.taskflow.domain.enums.Category;
import com.taskflow.domain.enums.Priority;
import com.taskflow.domain.enums.TaskStatus;
import com.taskflow.entity.TaskEntity;
import com.taskflow.entity.UserEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests de repositorio con @DataJpaTest.
 *
 * Concepto @DataJpaTest:
 * Levanta SOLO la capa JPA: entidades, repositorios y H2 en memoria.
 * No levanta controllers ni services. Cada test corre en una
 * transacción que se revierte al terminar — BD siempre limpia.
 *
 * Nota Spring Boot 4.x: TestEntityManager fue eliminado.
 * Se usa directamente userRepository.save() y taskRepository.save()
 * para insertar datos de prueba, lo cual es equivalente.
 *
 * @DataJpaTest omite Flyway — las secuencias users_seq/tasks_seq no se crean.
 * Con ddl-auto=create-drop, Hibernate genera el esquema (tablas + secuencias)
 * directamente desde las entidades, resolviendo el error "Sequence not found".
 */
@DataJpaTest
@ActiveProfiles("dev")
@TestPropertySource(properties = "spring.jpa.hibernate.ddl-auto=create-drop")
@DisplayName("TaskRepository — Tests @DataJpaTest")
class TaskRepositoryTest {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UserRepository userRepository;

    private UserEntity usuario;

    @BeforeEach
    void setUp() {
        // Persistir usuario de prueba usando el repositorio directamente
        usuario = userRepository.save(new UserEntity(
                "Ana García", "ana@test.com",
                "$2a$10$hash", UserEntity.Rol.USER));
    }

    // ── findByStatus ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("findByStatus: retorna solo tareas con el estado dado")
    void findByStatus_retornaTareasConEseEstado() {
        TaskEntity t1 = new TaskEntity("Tarea pendiente", Priority.ALTA, Category.TRABAJO, usuario);
        t1.setStatus(TaskStatus.PENDIENTE);
        TaskEntity t2 = new TaskEntity("Tarea completada", Priority.BAJA, Category.PERSONAL, usuario);
        t2.setStatus(TaskStatus.COMPLETADA);
        taskRepository.save(t1);
        taskRepository.save(t2);

        var resultado = taskRepository.findByStatus(TaskStatus.PENDIENTE,
                org.springframework.data.domain.Pageable.unpaged());

        assertThat(resultado.getContent()).hasSize(1);
        assertThat(resultado.getContent().get(0).getTitulo()).isEqualTo("Tarea pendiente");
    }

    // ── findByTituloContainingIgnoreCase ──────────────────────────────────────

    @Test
    @DisplayName("findByTituloContainingIgnoreCase: búsqueda insensible a mayúsculas")
    void findByTitulo_insensibleMayusculas() {
        taskRepository.save(new TaskEntity("Preparar Informe", Priority.MEDIA, Category.TRABAJO, usuario));

        var resultado = taskRepository.findByTituloContainingIgnoreCase("informe",
                org.springframework.data.domain.Pageable.unpaged());

        assertThat(resultado.getContent()).hasSize(1);
        assertThat(resultado.getContent().get(0).getTitulo()).isEqualTo("Preparar Informe");
    }

    // ── findVencidas ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("findVencidas: retorna tareas con fechaLimite pasada y estado activo")
    void findVencidas_retornaTareasVencidas() {
        TaskEntity vencida = new TaskEntity("Tarea vencida", Priority.ALTA, Category.TRABAJO, usuario);
        vencida.setFechaLimite(LocalDate.now().minusDays(5));
        vencida.setStatus(TaskStatus.PENDIENTE);

        TaskEntity noVencida = new TaskEntity("Tarea futura", Priority.BAJA, Category.PERSONAL, usuario);
        noVencida.setFechaLimite(LocalDate.now().plusDays(10));
        noVencida.setStatus(TaskStatus.PENDIENTE);

        taskRepository.save(vencida);
        taskRepository.save(noVencida);

        List<TaskEntity> resultado = taskRepository.findVencidas(
                LocalDate.now(),
                List.of(TaskStatus.PENDIENTE, TaskStatus.EN_PROGRESO));

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getTitulo()).isEqualTo("Tarea vencida");
    }

    // ── contarPorEstado ───────────────────────────────────────────────────────

    @Test
    @DisplayName("contarPorEstado: agrupa y cuenta correctamente por estado")
    void contarPorEstado_agrupaCorrecto() {
        TaskEntity t1 = new TaskEntity("T1", Priority.ALTA, Category.TRABAJO, usuario);
        t1.setStatus(TaskStatus.PENDIENTE);
        TaskEntity t2 = new TaskEntity("T2", Priority.MEDIA, Category.ESTUDIO, usuario);
        t2.setStatus(TaskStatus.PENDIENTE);
        TaskEntity t3 = new TaskEntity("T3", Priority.BAJA, Category.PERSONAL, usuario);
        t3.setStatus(TaskStatus.COMPLETADA);

        taskRepository.save(t1);
        taskRepository.save(t2);
        taskRepository.save(t3);

        List<Object[]> resultado = taskRepository.contarPorEstado();

        assertThat(resultado).isNotEmpty();
        long totalPendientes = resultado.stream()
                .filter(r -> r[0] == TaskStatus.PENDIENTE)
                .mapToLong(r -> (Long) r[1])
                .sum();
        assertThat(totalPendientes).isEqualTo(2);
    }

    // ── countByUsuarioIdAndStatus ─────────────────────────────────────────────

    @Test
    @DisplayName("countByUsuarioIdAndStatus: cuenta solo tareas del usuario dado")
    void countByUsuarioIdAndStatus_soloDelUsuario() {
        TaskEntity t1 = new TaskEntity("T1", Priority.ALTA, Category.TRABAJO, usuario);
        t1.setStatus(TaskStatus.PENDIENTE);
        TaskEntity t2 = new TaskEntity("T2", Priority.MEDIA, Category.ESTUDIO, usuario);
        t2.setStatus(TaskStatus.EN_PROGRESO);
        taskRepository.save(t1);
        taskRepository.save(t2);

        long count = taskRepository.countByUsuarioIdAndStatus(
                usuario.getId(), TaskStatus.PENDIENTE);

        assertThat(count).isEqualTo(1);
    }
}